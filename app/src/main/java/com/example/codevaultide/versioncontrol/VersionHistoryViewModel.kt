package com.example.codevaultide.versioncontrol

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codevaultide.database.AppDatabase
import com.example.codevaultide.database.VersionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

/**
 * Persistent version-control state backed by Room.
 * History survives ViewModel recreation and application restarts.
 */
class VersionHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val versionDao = db.versionDao()

    private val currentFileId = MutableStateFlow<Long?>(null)

    val versions: Flow<List<VersionEntity>> = currentFileId.flatMapLatest { id ->
        if (id == null) kotlinx.coroutines.flow.flowOf(emptyList())
        else versionDao.getVersionsForFile(id)
    }

    fun setFileId(id: Long?) {
        currentFileId.value = id
    }

    fun createVersion(fileId: Long, content: String, description: String = "Manual Save") {
        viewModelScope.launch(Dispatchers.IO) {
            val latest = versionDao.getLatestVersionAnyBranch(fileId)
            if (latest?.content == content) return@launch

            val nextNumber = (latest?.versionNumber ?: 0) + 1
            versionDao.insertVersion(
                VersionEntity(
                    fileId = fileId,
                    versionNumber = nextNumber,
                    parentVersionId = latest?.id,
                    branchName = latest?.branchName ?: "main",
                    content = content,
                    description = description,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
