package com.example.codevaultide.storage

import android.content.Context
import java.io.File

class FileManager(private val context: Context) {

    fun saveFile(filename: String, content: String): File {
        val file = File(context.filesDir, filename)
        file.writeText(content)
        return file
    }

    fun readFile(filename: String): String? {
        val file = File(context.filesDir, filename)
        return if (file.exists()) {
            file.readText()
        } else {
            null
        }
    }

    fun deleteFile(filename: String): Boolean {
        val file = File(context.filesDir, filename)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    fun listFiles(): Array<File>? {
        return context.filesDir.listFiles()
    }
}
