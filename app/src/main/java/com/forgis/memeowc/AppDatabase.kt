package com.forgis.memeowc

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WC::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wcDao(): WCDao
}