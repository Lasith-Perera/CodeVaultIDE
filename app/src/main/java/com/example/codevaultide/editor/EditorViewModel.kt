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

    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()

    fun setFileId(id: Long?) {
        _fileId.value = id
    }

    fun loadFile(id: Long?, name: String, content: String) {
        _fileId.value = id
        _fileName.value = name
        _code.value = content
        undoStack.clear()
        redoStack.clear()
    }

    fun updateCode(newCode: String) {
        if (newCode != _code.value) {
            undoStack.addLast(_code.value)
            _code.value = newCode
            redoStack.clear()
        }
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

    fun setCode(value: String) {
        _code.value = value
    }

    fun setFileName(name: String) {
        _fileName.value = name
    }

    fun rollbackToFileVersion(version: VersionEntity) {
        updateCode(version.deltaText)
    }

    fun rollbackToFileVersion(versionContent: String) {
        updateCode(versionContent)
    }
}
