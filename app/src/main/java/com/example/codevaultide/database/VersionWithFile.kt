package com.example.codevaultide.database

data class VersionWithFile(
    val id: Long,
    val fileId: Long,
    val versionNumber: Int,
    val parentVersionId: Long?,
    val branchName: String,
    val snapshotContent: String?,
    val content: String?,
    val deltaPatch: String?,
    val storageType: String,
    val description: String,
    val storedFileName: String?,
    val timestamp: Long,
    val fileName: String?
)
