package com.example.codevaultide.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "versions"
)
data class VersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val fileId: Long,

    val versionNumber: Int,

    val deltaText: String,

    val description: String,

    val timestamp: Long
)
