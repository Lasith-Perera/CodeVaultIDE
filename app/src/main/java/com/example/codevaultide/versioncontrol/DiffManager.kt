package com.example.codevaultide.versioncontrol

enum class DiffType {
    ADDED, REMOVED, UNCHANGED
}

data class DiffLine(
    val content: String,
    val type: DiffType,
    val lineNumber: Int? = null
)

class DiffManager {

    /**
     * Compares two strings and returns a list of DiffLine objects.
     * This is a simple line-based diff algorithm for Step 11.
     */
    fun computeDiff(oldText: String, newText: String): List<DiffLine> {
        val oldLines = oldText.lines()
        val newLines = newText.lines()
        val diffResult = mutableListOf<DiffLine>()

        // For simplicity, we use a basic comparison. 
        // A production app would use Myers' diff algorithm.
        
        val maxLines = maxOf(oldLines.size, newLines.size)
        
        for (i in 0 until maxLines) {
            val oldLine = oldLines.getOrNull(i)
            val newLine = newLines.getOrNull(i)
            
            when {
                oldLine == newLine && oldLine != null -> {
                    diffResult.add(DiffLine(oldLine, DiffType.UNCHANGED, i + 1))
                }
                oldLine != null && newLine != null -> {
                    // Line modified: treat as remove old + add new
                    diffResult.add(DiffLine(oldLine, DiffType.REMOVED, i + 1))
                    diffResult.add(DiffLine(newLine, DiffType.ADDED, i + 1))
                }
                oldLine != null -> {
                    diffResult.add(DiffLine(oldLine, DiffType.REMOVED, i + 1))
                }
                newLine != null -> {
                    diffResult.add(DiffLine(newLine, DiffType.ADDED, i + 1))
                }
            }
        }
        
        return diffResult
    }
}
