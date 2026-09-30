package com.example.codevaultide.compiler

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
class CompilerManager {
    companion object {
        const val DEFAULT_BASE_URL = "https://ce.judge0.com"
    }

    private val baseUrl = DEFAULT_BASE_URL.trimEnd('/')

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun compileAndRun(
        language: String,
        code: String,
        stdin: String = ""
    ): String = withContext(Dispatchers.IO) {
        compileOnline(language, code, stdin)
    }

    fun executionMode(@Suppress("UNUSED_PARAMETER") language: String): String = "ONLINE"

    private suspend fun compileOnline(language: String, code: String, stdin: String): String {
        val languageId = languageToId(language)
        if (languageId == -1) {
            return "Unsupported language: $language\nThis IDE uses the online compiler API for every language."
        }

        return try {
            val encodedCode = encode(code)
            val encodedStdin = encode(stdin)
            val bodyJson = JSONObject().apply {
                put("language_id", languageId)
                put("source_code", encodedCode)
                put("stdin", encodedStdin)
                put("cpu_time_limit", 5)
                put("wall_time_limit", 10)
                put("memory_limit", 128000)
            }

            val submitUrl = "$baseUrl/submissions?base64_encoded=true&wait=false"
            val submitRequest = Request.Builder()
                .url(submitUrl)
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val token = client.newCall(submitRequest).execute().use { response ->
                if (!response.isSuccessful) {
                    val detail = response.body?.string().orEmpty()
                    return "API submission failed (${response.code} ${response.message})" +
                            if (detail.isNotBlank()) "\n$detail" else ""
                }
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) return "Empty response from compiler API."
                JSONObject(body).optString("token").ifBlank {
                    return "Compiler API did not return a submission token.\n$body"
                }
            }

            repeat(60) {
                delay(500)
                val url = "$baseUrl/submissions/$token?base64_encoded=true"
                val request = Request.Builder().url(url).get().build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return "API polling failed (${response.code} ${response.message})"
                    }

                    val json = JSONObject(response.body?.string().orEmpty())
                    val statusId = json.optJSONObject("status")?.optInt("id", -1) ?: -1
                    if (statusId in 1..2) return@use

                    return formatResult(json)
                }
            }

            "API execution timeout: the compiler server did not finish within 30 seconds."
        } catch (e: Exception) {
            "ONLINE COMPILER ERROR:\n${e.localizedMessage ?: e.javaClass.simpleName}"
        }
    }

    private fun formatResult(json: JSONObject): String {
        val stdout = decode(json.optString("stdout"))
        val stderr = decode(json.optString("stderr"))
        val rawCompileOutput = decode(json.optString("compile_output"))
        val message = decode(json.optString("message"))
        val statusObj = json.optJSONObject("status")
        val statusId = statusObj?.optInt("id", -1) ?: -1
        val status = statusObj?.optString("description").orEmpty()

        // Judge0 status 3 = Accepted. On a successful run, stdout is the
        // program result. Some Kotlin/JVM runtimes emit harmless launcher
        // warnings to stderr/compile_output; do not show those as errors.
        if (statusId == 3) {
            return if (stdout.isNotBlank()) {
                stdout.trimEnd()
            } else {
                "Program finished successfully with no output."
            }
        }

        val compileOutput = rawCompileOutput
            .lines()
            .filterNot { line ->
                line.contains("OpenJDK 64-Bit Server VM warning", ignoreCase = true) ||
                        line.contains("WARNING: A restricted method", ignoreCase = true) ||
                        line.contains("java.lang.System::load", ignoreCase = true) ||
                        line.contains("org.fusesource.jansi", ignoreCase = true) ||
                        line.contains("restricted methods will be blocked", ignoreCase = true) ||
                        line.contains("-Xverify:none", ignoreCase = true) ||
                        line.contains("-noverify", ignoreCase = true)
            }
            .joinToString("\n")
            .trim()

        return when {
            // Judge0 status 6 = Compilation Error
            statusId == 6 -> {
                val details = when {
                    compileOutput.isNotBlank() -> compileOutput
                    stderr.isNotBlank() -> stderr
                    message.isNotBlank() -> message
                    else -> "Compilation failed."
                }
                "COMPILE ERROR${if (status.isNotBlank()) " [$status]" else ""}:\n$details"
            }

            // Judge0 statuses 7-12 are runtime errors.
            statusId in 7..12 -> {
                val details = when {
                    stderr.isNotBlank() -> stderr
                    message.isNotBlank() -> message
                    stdout.isNotBlank() -> stdout
                    else -> "Runtime execution failed."
                }
                "RUNTIME ERROR${if (status.isNotBlank()) " [$status]" else ""}:\n$details"
            }

            // Status 5 = Time Limit Exceeded; 13/14 are Judge0 execution errors.
            statusId == 5 -> "EXECUTION ERROR [${status.ifBlank { "Time Limit Exceeded" }}]"
            statusId == 13 || statusId == 14 -> {
                val details = when {
                    message.isNotBlank() -> message
                    stderr.isNotBlank() -> stderr
                    compileOutput.isNotBlank() -> compileOutput
                    else -> "Compiler server execution error."
                }
                "EXECUTION ERROR${if (status.isNotBlank()) " [$status]" else ""}:\n$details"
            }

            compileOutput.isNotBlank() -> "COMPILER OUTPUT:\n$compileOutput"
            stderr.isNotBlank() -> stderr
            stdout.isNotBlank() -> stdout.trimEnd()
            message.isNotBlank() -> message
            status.isNotBlank() -> "[$status]"
            else -> "Program finished with no output."
        }
    }

    private fun encode(value: String): String =
        Base64.encodeToString(value.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

    private fun decode(value: String): String = try {
        if (value.isBlank() || value == "null") ""
        else String(Base64.decode(value, Base64.DEFAULT), StandardCharsets.UTF_8)
    } catch (_: Exception) {
        value
    }

    private suspend fun languageToId(language: String): Int {
        val requested = normalizeLanguage(language)
        val aliases = languageAliases(requested)

        return try {
            val request = Request.Builder()
                .url("$baseUrl/languages/")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return -1
                val array = org.json.JSONArray(response.body?.string().orEmpty())
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    val id = item.optInt("id", -1)
                    val name = item.optString("name").lowercase()
                    if (id > 0 && aliases.any { alias -> name.startsWith(alias) || name.contains("($alias") }) {
                        return id
                    }
                }
                -1
            }
        } catch (_: Exception) {
            -1
        }
    }

    private fun normalizeLanguage(language: String): String =
        language.lowercase().trim().removePrefix(".")

    private fun languageAliases(language: String): Set<String> = when (language) {
        "kt", "kts", "kotlin" -> setOf("kotlin")
        "cpp", "c++" -> setOf("c++")
        "c" -> setOf("c ", "c(")
        "py", "python", "python3" -> setOf("python")
        "js", "javascript" -> setOf("javascript")
        "ts", "typescript" -> setOf("typescript")
        "java" -> setOf("java")
        "cs", "c#", "csharp" -> setOf("c#", "csharp")
        "rs", "rust" -> setOf("rust")
        "go" -> setOf("go")
        "swift" -> setOf("swift")
        "php" -> setOf("php")
        "rb", "ruby" -> setOf("ruby")
        "bash", "sh" -> setOf("bash")
        "lua" -> setOf("lua")
        "perl", "pl" -> setOf("perl")
        "r" -> setOf("r ", "r(")
        "scala" -> setOf("scala")
        "dart" -> setOf("dart")
        "groovy" -> setOf("groovy")
        else -> setOf(language)
    }
}