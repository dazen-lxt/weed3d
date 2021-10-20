package com.ncsu.weed3d.data

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseInstance {

    private var databaseInstance: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {

        return databaseInstance ?: run {
            databaseInstance = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "database-name"
            )
                .fallbackToDestructiveMigration()
                .build()
            databaseInstance!!
        }
    }


}