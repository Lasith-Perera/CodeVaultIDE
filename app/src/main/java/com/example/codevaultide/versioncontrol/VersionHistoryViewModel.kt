package com.example.codevaultide.versioncontrol

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.room.Room
import com.example.codevaultide.database.AppDatabase
import com.example.codevaultide.database.VersionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest

class VersionHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "codevault-db"
    ).build()
    
    private val versionDao = db.versionDao()

    private val _currentFileId = MutableStateFlow<Long>(0L)

    val versions: Flow<List<VersionEntity>> = _currentFileId.flatMapLatest { id ->
        versionDao.getVersionsForFile(id)
    }

    fun setFileId(id: Long) {
        _currentFileId.value = id
    }
}
