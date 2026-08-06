package com.example.codevaultide.editor

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EditorViewModel : ViewModel() {
    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    private val _fileName = MutableStateFlow("Main.kt")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    fun updateCode(newCode: String) {
        _code.value = newCode
    }

    fun setCode(newCode: String) {
        _code.value = newCode
    }

    fun setFileName(name: String) {
        _fileName.value = name
    }

    // Rollback functionality for HistoryScreen
    fun rollbackToFileVersion(versionContent: String) {
        _code.value = versionContent
    }

    fun undo() { /* Undo Logic */ }
    fun redo() { /* Redo Logic */ }
}