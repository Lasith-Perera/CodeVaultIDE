package com.example.codevaultide.editor

import androidx.lifecycle.ViewModel
import com.example.codevaultide.database.VersionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditorViewModel : ViewModel() {

    private val _code = MutableStateFlow(
        """
        package com.example.codevault

        fun main() {
            println("Hello CodeVault IDE")
        }
        """.trimIndent()
    )
    val code: StateFlow<String> = _code.asStateFlow()

    private val _fileName = MutableStateFlow("Main.kt")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _fileId = MutableStateFlow<Long?>(null)
    val fileId: StateFlow<Long?> = _fileId.asStateFlow()

    private val _cursorPosition = MutableStateFlow(0)
    val cursorPosition: StateFlow<Int> = _cursorPosition.asStateFlow()

    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()

    fun updateCode(newCode: String) {
        if (newCode != _code.value) {
            undoStack.addLast(_code.value)
            _code.value = newCode
            redoStack.clear()
        }
    }

    fun setFileName(name: String) {
        _fileName.value = name
    }

    fun setFileId(id: Long?) {
        _fileId.value = id
    }

    fun setCode(value: String) {
        _code.value = value
    }

    fun loadFile(id: Long?, name: String, content: String) {
        _fileId.value = id
        _fileName.value = name
        _code.value = content
        undoStack.clear()
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.addLast(_code.value)
            _code.value = undoStack.removeLast()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.addLast(_code.value)
            _code.value = redoStack.removeLast()
        }
    }

    fun rollbackToFileVersion(versionContent: String) {
        updateCode(versionContent)
    }

    fun rollbackToFileVersion(version: VersionEntity) {
        updateCode(version.deltaText)
    }

    fun updateCursorPosition(position: Int) {
        _cursorPosition.value = position
    }

    // --- AI Integration Functions ---

    /**
     * Completely replaces current editor text with AI generated or fixed code.
     */
    fun applyAiGeneratedCode(generatedCode: String) {
        updateCode(generatedCode)
    }

    /**
     * Inserts AI generated snippet at the current cursor position instead of overwriting the file.
     */
    fun insertAiCodeAtCursor(aiSnippet: String) {
        val currentText = _code.value
        val pos = _cursorPosition.value.coerceIn(0, currentText.length)

        val newText = StringBuilder(currentText)
            .insert(pos, "\n$aiSnippet\n")
            .toString()

        updateCode(newText)
    }

    /**
     * Clears all text inside the active file buffer.
     */
    fun clearEditor() {
        updateCode("")
    }
}
