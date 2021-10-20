package com.ncsu.weed3d.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [VideoData::class, FarmData::class, SensorTimeData::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun databaseDao(): DatabaseDao
}