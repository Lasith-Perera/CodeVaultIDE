package com.example.codevaultide.editor

import android.content.Context
import org.json.JSONObject

data class SyntaxRules(
    val keywords: List<String>,
    val colors: Map<String, String>
) {
    companion object {
        fun load(context: Context, language: String): SyntaxRules {
            return try {
                val jsonString = context.assets.open("syntax/$language.json").bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)
                val keywordsList = mutableListOf<String>()
                val keywordsArray = json.getJSONArray("keywords")
                for (i in 0 until keywordsArray.length()) {
                    keywordsList.add(keywordsArray.getString(i))
                }
                val colorsMap = mutableMapOf<String, String>()
                val colorsJson = json.getJSONObject("colors")
                colorsJson.keys().forEach { key ->
                    colorsMap[key] = colorsJson.getString(key)
                }
                SyntaxRules(keywordsList, colorsMap)
            } catch (e: Exception) {
                // Fallback rules
                SyntaxRules(
                    keywords = listOf("fun", "val", "var", "class", "if", "else", "return"),
                    colors = mapOf(
                        "keywords" to "#C678DD",
                        "strings" to "#98C379",
                        "comments" to "#5C6370"
                    )
                )
            }
        }
    }
}
