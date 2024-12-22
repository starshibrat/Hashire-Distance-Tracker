package com.edu.hashire_distancetrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.Date

@Entity(tableName = "runs")
data class Run (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val distance: Double = 0.0,
    val description: String = "",
    val speed: Double = 0.0,
    val time: Long = 0,
    val createdAt: Date = Date(),
    )

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

}