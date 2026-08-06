package com.example.codevaultide.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files ORDER BY lastModified DESC")
    fun getAllFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun getFileById(id: Long): FileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
<<<<<<< HEAD
    suspend fun insertFile(file: FileEntity)
=======
    suspend fun insertFile(file: FileEntity): Long
>>>>>>> origin/main

    @Update
    suspend fun updateFile(file: FileEntity)

<<<<<<< HEAD
    @Delete
    suspend fun deleteFile(file: FileEntity)
}
=======
    @Query("UPDATE files SET name = :newName, lastModified = :lastModified WHERE id = :id")
    suspend fun updateFileName(id: Long, newName: String, lastModified: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("DELETE FROM files WHERE id IN (:ids)")
    suspend fun deleteFilesByIds(ids: List<Long>)
}
>>>>>>> origin/main
