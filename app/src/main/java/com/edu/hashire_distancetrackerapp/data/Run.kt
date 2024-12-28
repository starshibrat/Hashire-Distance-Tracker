package com.edu.hashire_distancetrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    val coordinates: List<Pair<Double, Double>> = listOf()
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

    @TypeConverter
    fun fromString(value: String): List<Pair<Double,Double>>{
        val type = object : TypeToken<List<Pair<Double, Double>>>(){}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList (list: List<Pair<Double, Double>>): String? {
        return Gson().toJson(list)
    }


}