package com.example.codevaultide.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FileEntity::class, VersionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun versionDao(): VersionDao
}
