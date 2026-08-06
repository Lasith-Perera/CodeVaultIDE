package com.example.codevaultide.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codevaultide.database.FileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FileViewModel : ViewModel() {
    private val _allFiles = MutableStateFlow<List<FileEntity>>(emptyList())
    val allFiles: StateFlow<List<FileEntity>> = _allFiles.asStateFlow()

    fun createNewFile(name: String, content: String) {
        viewModelScope.launch {
            val newFile = FileEntity(
                id = System.currentTimeMillis(),
                name = name,
                path = "/workspace/$name", // <-- මෙතැනට path එක එකතු කරන්න
                content = content,
                lastModified = System.currentTimeMillis()
            )
            _allFiles.value = _allFiles.value + newFile
        }
    }
}