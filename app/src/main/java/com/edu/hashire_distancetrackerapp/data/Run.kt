package com.edu.hashire_distancetrackerapp.data

import java.util.Date


data class Run (
    val id: Int = 0,
    val title: String = "",
    val distance: Double = 0.0,
    val description: String = "",
    val speed: Double = 0.0,
    val time: Long = 0,
    val createdAt: Date = Date(),
    )