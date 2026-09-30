package com.example.codevaultide.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "versions",
    indices = [
        Index(value = ["fileId", "branchName", "versionNumber"], unique = true),
        Index(value = ["fileId", "timestamp"])
    ]
)
data class VersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileId: Long,
    val versionNumber: Int,
    val parentVersionId: Long? = null,
    val branchName: String = "main",
    /** Full source is stored only for BASE/CHECKPOINT versions. */
    val snapshotContent: String? = null,
    /** Legacy v5 field; new versions leave this null. */
    val content: String? = null,
    /** Unified diff patch from parentVersionId to this version. */
    val deltaPatch: String? = null,
    /** BASE or DELTA. */
    val storageType: String = STORAGE_DELTA,
    val description: String,
    val storedFileName: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val STORAGE_BASE = "BASE"
        const val STORAGE_DELTA = "DELTA"
    }
}
