package com.example.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.database.dao.AnnouncementDao
import com.example.data.database.entity.AnnouncementEntity

@Database(
    entities = [AnnouncementEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun announcementDao(): AnnouncementDao

    companion object {
        const val DATABASE_NAME = "timechime_db"
    }
}
