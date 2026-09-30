package com.example.codevaultide.versioncontrol

import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import com.github.difflib.patch.Patch

/** Uses java-diff-utils instead of a hand-written diff algorithm. */
class DiffManager {
    fun createPatch(oldText: String, newText: String): String {
        val oldLines = splitLines(oldText)
        val newLines = splitLines(newText)
        val patch = DiffUtils.diff(oldLines, newLines)
        return UnifiedDiffUtils.generateUnifiedDiff(
            "parent",
            "version",
            oldLines,
            patch,
            3
        ).joinToString("\n")
    }

    fun applyPatch(oldText: String, patchText: String): String {
        if (patchText.isBlank()) return oldText
        val source = splitLines(oldText)
        val unified = patchText.lines()
        val patch: Patch<String> = UnifiedDiffUtils.parseUnifiedDiff(unified)
        return DiffUtils.patch(source, patch).joinToString("\n")
    }

    fun computeDiff(oldText: String, newText: String): List<DiffLine> {
        val oldLines = splitLines(oldText)
        val newLines = splitLines(newText)
        val patch = DiffUtils.diff(oldLines, newLines)
        val result = mutableListOf<DiffLine>()
        var lineNumber = 1
        var oldCursor = 0
        var newCursor = 0

        for (delta in patch.deltas) {
            while (oldCursor < delta.source.position && newCursor < delta.target.position) {
                result += DiffLine(oldLines[oldCursor], DiffType.UNCHANGED, lineNumber++)
                oldCursor++
                newCursor++
            }
            while (oldCursor < delta.source.position) {
                result += DiffLine(oldLines[oldCursor++], DiffType.REMOVED, lineNumber)
            }
            delta.source.lines.forEach { result += DiffLine(it, DiffType.REMOVED, lineNumber) }
            delta.target.lines.forEach { result += DiffLine(it, DiffType.ADDED, lineNumber) }
            oldCursor = delta.source.position + delta.source.size()
            newCursor = delta.target.position + delta.target.size()
        }
        while (oldCursor < oldLines.size && newCursor < newLines.size) {
            result += DiffLine(oldLines[oldCursor++], DiffType.UNCHANGED, lineNumber++)
            newCursor++
        }
        while (oldCursor < oldLines.size) result += DiffLine(oldLines[oldCursor++], DiffType.REMOVED, lineNumber)
        while (newCursor < newLines.size) result += DiffLine(newLines[newCursor++], DiffType.ADDED, lineNumber++)
        return result
    }

    private fun splitLines(text: String): List<String> =
        if (text.isEmpty()) emptyList() else text.replace("\r\n", "\n").split('\n')
}

enum class DiffType { ADDED, REMOVED, UNCHANGED }
data class DiffLine(val content: String, val type: DiffType, val lineNumber: Int? = null)
