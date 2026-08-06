package com.example.codevaultide.versioncontrol

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codevaultide.database.AppDatabase
import com.example.codevaultide.database.VersionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class VersionHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val versionDao = db.versionDao()

    private val _currentFileId = MutableStateFlow<Long>(0L)
    val currentFileId: Long get() = _currentFileId.value

    @OptIn(ExperimentalCoroutinesApi::class)
    val versions: Flow<List<VersionEntity>> = _currentFileId.flatMapLatest { id ->
        versionDao.getVersionsForFile(id)
    }

    fun setFileId(id: Long) {
        _currentFileId.value = id
    }

    fun saveVersionSnapshot(fileId: Long, content: String, description: String = "Auto-snapshot") {
        if (fileId == 0L) return
        
        viewModelScope.launch(Dispatchers.IO) {
            val count = versionDao.getVersionCount(fileId)
            val version = VersionEntity(
                fileId = fileId,
                versionNumber = count + 1,
                deltaText = content, // Storing full content for simplicity in this version
                description = description,
                timestamp = System.currentTimeMillis()
            )
            versionDao.insertVersion(version)
        }
    }
}
