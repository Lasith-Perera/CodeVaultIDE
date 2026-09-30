package com.example.codevaultide.editor

data class Snapshot(
    val id: Long,
    val fileId: Long,
    val fileName: String,
    val content: String,
    val message: String,
    val timestamp: Long
)