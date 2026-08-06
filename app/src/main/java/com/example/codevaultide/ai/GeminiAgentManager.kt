package com.example.codevaultide.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

object GeminiAgentManager {

    var apiKey: String = ""

    private fun getGenerativeModel(): GenerativeModel? {
        if (apiKey.isBlank()) return null

        return GenerativeModel(
            modelName = "gemini-3.6-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.1f
            }
        )
    }

    /**
     * Cleans raw AI response text by stripping out Markdown code blocks, headers, and extra prose.
     */
    fun cleanCodeOutput(rawOutput: String): String {
        var clean = rawOutput.trim()

        // Remove starting markdown code fence blocks if present
        if (clean.contains("```")) {
            val lines = clean.lines()
            val filteredLines = lines.filterNot { line ->
                line.trim().startsWith("```") ||
                        line.trim().startsWith("###") ||
                        line.trim().startsWith("---")
            }
            clean = filteredLines.joinToString("\n").trim()
        }

        return clean
    }

    /**
     * Streams AI agent responses in real time using the Gemini API SDK.
     */
    fun generateCodeResponseStream(prompt: String, activeCodeContext: String = ""): Flow<GenerateContentResponse> {
        val model = getGenerativeModel() ?: return emptyFlow()

        val agentSystemPrompt = """
            You are CodeVault AI, an automated code generator and debugger built directly into a code editor.
            
            CRITICAL RULES FOR OUTPUT:
            1. Output ONLY valid, executable source code.
            2. Do NOT write conversational intros, preambles, or explanations (e.g., DO NOT write "Here is the code...").
            3. Do NOT wrap code in Markdown backticks (```) or headings (###).
            4. Output pure raw source code ready to be pasted directly into an IDE file.
            
            ${if (activeCodeContext.isNotBlank()) "Active File Code Context:\n$activeCodeContext\n" else ""}
            User Task: $prompt
        """.trimIndent()

        return model.generateContentStream(agentSystemPrompt)
    }
}