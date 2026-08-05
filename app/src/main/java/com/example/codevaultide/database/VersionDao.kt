package com.example.codevaultide.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VersionDao {
    @Query("SELECT * FROM versions WHERE fileId = :fileId ORDER BY timestamp DESC")
    fun getVersionsForFile(fileId: Long): Flow<List<VersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: VersionEntity)

    @Query("SELECT COUNT(*) FROM versions WHERE fileId = :fileId")
    suspend fun getVersionCount(fileId: Long): Int
}
