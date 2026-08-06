package com.example.codevaultide.compiler

<<<<<<< HEAD
import kotlinx.coroutines.delay

/**
 * Simulates a Kotlin compiler service.
 */
class CompilerManager {

    /**
     * Simulates compiling the given [code].
     * Returns a pair of (IsSuccess, OutputMessage)
     */
    suspend fun compileAndRun(code: String): Pair<Boolean, String> {
        // Simulate network/process delay
        delay(1500)

        return when {
            code.isBlank() -> {
                false to "Error: No code provided to the compiler."
            }
            code.contains("fun main()") -> {
                true to """
                    > Starting compilation...
                    > Successfully compiled Main.kt
                    > Running...
                    
                    Hello from CodeVault IDE!
                    
                    Process finished with exit code 0
                """.trimIndent()
            }
            else -> {
                false to """
                    > Starting compilation...
                    e: Main.kt: (1, 1): Expecting a 'fun main()' entry point.
                    
                    Compilation failed with 1 error
                """.trimIndent()
            }
        }
    }
}
=======
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

class CompilerManager {

    companion object {
        private const val BASE_URL = "https://ce.judge0.com"
    }

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

        val languageId = languageToId(language)

        if (languageId == -1)
            return@withContext "Unsupported language: $language"

        try {
            // Encode Base64 to prevent null/character escaping issues
            val encodedCode = Base64.encodeToString(code.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
            val encodedStdin = Base64.encodeToString(stdin.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

            val bodyJson = JSONObject().apply {
                put("language_id", languageId)
                put("source_code", encodedCode)
                put("stdin", encodedStdin)
            }

            val submitRequest = Request.Builder()
                .url("$BASE_URL/submissions?base64_encoded=true&wait=false")
                .post(
                    bodyJson.toString().toRequestBody(
                        "application/json".toMediaType()
                    )
                )
                .addHeader("Content-Type", "application/json")
                .build()

            val token = client.newCall(submitRequest)
                .execute()
                .use { response ->
                    if (!response.isSuccessful)
                        return@withContext "Submission failed (${response.code} ${response.message})"

                    val responseString = response.body?.string() ?: ""
                    if (responseString.isEmpty()) return@withContext "Empty response from compiler server."

                    val json = JSONObject(responseString)
                    json.getString("token")
                }

            repeat(30) {
                delay(1000)

                val pollRequest = Request.Builder()
                    .url(
                        "$BASE_URL/submissions/${
                            URLEncoder.encode(token, "UTF-8")
                        }?base64_encoded=true"
                    )
                    .build()

                val response = client.newCall(pollRequest).execute()

                response.use { res ->
                    if (!res.isSuccessful)
                        return@withContext "Polling failed (${res.code} ${res.message})"

                    val json = JSONObject(res.body!!.string())
                    val status = json.getJSONObject("status")
                    val statusId = status.getInt("id")

                    when (statusId) {
                        1, 2 -> {
                            // Processing / In Queue
                        }
                        else -> {
                            val stdout = decodeBase64(json.optString("stdout", ""))
                            val stderr = decodeBase64(json.optString("stderr", ""))
                            val compileOutput = decodeBase64(json.optString("compile_output", ""))
                            val message = decodeBase64(json.optString("message", ""))

                            return@withContext when {
                                compileOutput.isNotBlank() -> "COMPILE ERROR:\n$compileOutput"
                                stderr.isNotBlank() -> "RUNTIME ERROR:\n$stderr"
                                stdout.isNotBlank() -> stdout
                                message.isNotBlank() -> message
                                else -> "Program finished successfully with no output."
                            }
                        }
                    }
                }
            }

            "Execution timeout."

        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: "Unknown network/execution error"}"
        }
    }

    private fun decodeBase64(value: String): String {
        if (value.isEmpty() || value == "null") return ""
        return try {
            String(Base64.decode(value, Base64.DEFAULT), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            value
        }
    }

    private fun languageToId(language: String): Int {
        return when (language.lowercase().trim()) {
            "c" -> 50
            "cpp", "c++" -> 54
            "java" -> 62
            "python", "python3", "py" -> 71
            "javascript", "js" -> 63
            "typescript", "ts" -> 74
            "go" -> 60
            "rust" -> 73
            "kotlin", "kt" -> 78
            "swift" -> 83
            "php" -> 68
            "ruby" -> 72
            "c#" -> 51
            else -> -1
        }
    }
}
>>>>>>> origin/main
