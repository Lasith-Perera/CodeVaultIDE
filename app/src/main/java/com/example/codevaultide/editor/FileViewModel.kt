package com.example.codevaultide.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codevaultide.database.AppDatabase
import com.example.codevaultide.database.FileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FileViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val fileDao = db.fileDao()

    val allFiles: StateFlow<List<FileEntity>> = fileDao.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createNewFile(name: String, content: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = System.currentTimeMillis()
            val newFile = FileEntity(
                id = id,
                name = name,
                path = "/workspace/$name",
                content = content,
                lastModified = System.currentTimeMillis()
            )
            fileDao.insertFile(newFile)
            withContext(Dispatchers.Main) {
                onCreated(id)
            }
        }
    }

    fun updateFile(id: Long, name: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingFile = fileDao.getFileById(id)
            if (existingFile != null) {
                val updatedFile = existingFile.copy(
                    name = name,
                    content = content,
                    lastModified = System.currentTimeMillis()
                )
                fileDao.updateFile(updatedFile)
            } else {
                val newFile = FileEntity(
                    id = id,
                    name = name,
                    path = "/workspace/$name",
                    content = content,
                    lastModified = System.currentTimeMillis()
                )
                fileDao.insertFile(newFile)
            }
        }
    }

    fun updateFileName(id: Long, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            fileDao.updateFileName(id = id, newName = newName, lastModified = System.currentTimeMillis())
        }
    }

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            fileDao.deleteFile(file)
        }
    }

    fun deleteFilesByIds(ids: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            fileDao.deleteFilesByIds(ids)
        }
    }
}