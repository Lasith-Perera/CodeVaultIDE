package com.example.codevaultide.editor

import androidx.lifecycle.ViewModel
import com.example.codevaultide.ui.screens.FileVersion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FileHistoryItem(
    val fileId: Long,
    val fileName: String,
    val versions: List<FileVersion>
)

class EditorViewModel : ViewModel() {
    private val _code = MutableStateFlow("")
    val code: StateFlow<String> = _code.asStateFlow()

    private val _fileName = MutableStateFlow("Main.kt")
    val fileName: StateFlow<String> = _fileName.asStateFlow()

    private val _fileId = MutableStateFlow<Long?>(null)
    val fileId: StateFlow<Long?> = _fileId.asStateFlow()

    private val _cursorPosition = MutableStateFlow(0)
    val cursorPosition: StateFlow<Int> = _cursorPosition.asStateFlow()

    // Map storing version history list keyed by fileId
    private val fileHistoryMap = mutableMapOf<Long, MutableList<FileVersion>>()
    private val fileNameMap = mutableMapOf<Long, String>()

    // Expose all files that have history
    private val _allFilesWithHistory = MutableStateFlow<List<FileHistoryItem>>(emptyList())
    val allFilesWithHistory: StateFlow<List<FileHistoryItem>> = _allFilesWithHistory.asStateFlow()

    // Selected file history for viewing
    private val _selectedFileHistory = MutableStateFlow<FileHistoryItem?>(null)
    val selectedFileHistory: StateFlow<FileHistoryItem?> = _selectedFileHistory.asStateFlow()

    private val _versionHistory = MutableStateFlow<List<FileVersion>>(emptyList())
    val versionHistory: StateFlow<List<FileVersion>> = _versionHistory.asStateFlow()

    fun setFileId(id: Long?) {
        _fileId.value = id
        id?.let { refreshHistoryForFile(it) }
    }

    fun loadFile(id: Long?, name: String, content: String) {
        _fileId.value = id
        _fileName.value = name
        _code.value = content

        if (id != null) {
            fileNameMap[id] = name
            if (!fileHistoryMap.containsKey(id)) {
                fileHistoryMap[id] = mutableListOf(
                    FileVersion(
                        id = System.currentTimeMillis(),
                        timestamp = getCurrentFormattedTime(),
                        summary = "Initial Version",
                        content = content
                    )
                )
            }
            refreshHistoryForFile(id)
            updateAllFilesWithHistory()
        } else {
            _versionHistory.value = emptyList()
        }
    }

    fun selectFileForHistory(fileId: Long) {
        val versions = fileHistoryMap[fileId] ?: emptyList()
        val name = fileNameMap[fileId] ?: "Unknown File"
        _selectedFileHistory.value = FileHistoryItem(fileId, name, versions)
    }

    fun clearSelectedFileHistory() {
        _selectedFileHistory.value = null
    }

    fun updateCode(newCode: String) {
        _code.value = newCode
    }

    fun saveVersionSnapshot(summary: String = "Manual Save") {
        val currentId = _fileId.value ?: return
        val currentContent = _code.value

        val newVersion = FileVersion(
            id = System.currentTimeMillis(),
            timestamp = getCurrentFormattedTime(),
            summary = summary,
            content = currentContent
        )

        val historyList = fileHistoryMap.getOrPut(currentId) { mutableListOf() }
        historyList.add(0, newVersion)
        refreshHistoryForFile(currentId)
        updateAllFilesWithHistory()
    }

    fun rollbackToFileVersion(versionContent: String) {
        _code.value = versionContent
    }

    private fun refreshHistoryForFile(id: Long) {
        val history = fileHistoryMap[id]?.toList() ?: emptyList()
        _versionHistory.value = history
        val name = fileNameMap[id] ?: _fileName.value
        _selectedFileHistory.value = FileHistoryItem(id, name, history)
    }

    private fun updateAllFilesWithHistory() {
        _allFilesWithHistory.value = fileHistoryMap.map { (id, versions) ->
            FileHistoryItem(
                fileId = id,
                fileName = fileNameMap[id] ?: "File $id",
                versions = versions.toList()
            )
        }
    }

    private fun getCurrentFormattedTime(): String {
        val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        return formatter.format(Date())
    }

    fun undo() {}
    fun redo() {}
    fun clearEditor() { _code.value = "" }
}