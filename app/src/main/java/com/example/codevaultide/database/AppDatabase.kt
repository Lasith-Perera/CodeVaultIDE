package com.example.codevaultide.database

<<<<<<< HEAD
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FileEntity::class, VersionEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun versionDao(): VersionDao
=======
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FileEntity::class, VersionEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun versionDao(): VersionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "codevault-db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
>>>>>>> origin/main
}
