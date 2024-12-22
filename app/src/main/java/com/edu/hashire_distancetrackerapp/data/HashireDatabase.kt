package com.edu.hashire_distancetrackerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Run::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HashireDatabase : RoomDatabase() {

    abstract fun runDao(): RunDao

    companion object {
        @Volatile
        private var Instance: HashireDatabase? = null

        fun getDatabase(context: Context): HashireDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, HashireDatabase::class.java, "hashire_database").build()
                    .also { Instance = it}
            }
        }

    }

}
