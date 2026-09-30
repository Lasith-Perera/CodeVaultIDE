package com.example.codevaultide.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VersionDao {
    @Query("SELECT * FROM versions WHERE fileId = :fileId ORDER BY timestamp DESC")
    fun getVersionsForFile(fileId: Long): Flow<List<VersionEntity>>

    @Query("SELECT * FROM versions WHERE fileId = :fileId ORDER BY versionNumber ASC")
    suspend fun getVersionsForFileSync(fileId: Long): List<VersionEntity>

    @Query("""
        SELECT v.id, v.fileId, v.versionNumber, v.parentVersionId, v.branchName,
               v.snapshotContent, v.content, v.deltaPatch, v.storageType, v.description,
               v.storedFileName, v.timestamp,
               f.name AS fileName
        FROM versions v
        LEFT JOIN files f ON f.id = v.fileId
        ORDER BY v.timestamp DESC
    """)
    fun getAllVersionsWithFileName(): Flow<List<VersionWithFile>>

    @Query("SELECT * FROM versions WHERE fileId = :fileId AND branchName = :branchName ORDER BY versionNumber DESC LIMIT 1")
    suspend fun getLatestVersion(fileId: Long, branchName: String): VersionEntity?

    @Query("SELECT * FROM versions WHERE fileId = :fileId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestVersionAnyBranch(fileId: Long): VersionEntity?

    @Query("SELECT * FROM versions WHERE id = :versionId LIMIT 1")
    suspend fun getVersionById(versionId: Long): VersionEntity?

    @Query("SELECT * FROM versions WHERE fileId = :fileId AND branchName = :branchName ORDER BY versionNumber ASC")
    suspend fun getVersionsForBranch(fileId: Long, branchName: String): List<VersionEntity>

    @Query("SELECT DISTINCT branchName FROM versions WHERE fileId = :fileId ORDER BY branchName")
    suspend fun getBranches(fileId: Long): List<String>

    @Query("SELECT COUNT(*) FROM versions WHERE fileId = :fileId AND branchName = :branchName")
    suspend fun getVersionCount(fileId: Long, branchName: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVersion(version: VersionEntity): Long

    @Query("DELETE FROM versions WHERE id = :id")
    suspend fun deleteVersionById(id: Long)

    @Query("DELETE FROM versions WHERE fileId = :fileId")
    suspend fun deleteVersionsForFile(fileId: Long)
}
