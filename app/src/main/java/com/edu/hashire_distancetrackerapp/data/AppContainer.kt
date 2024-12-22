package com.edu.hashire_distancetrackerapp.data

import android.content.Context

interface AppContainer {
    val runsRepository: RunsRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val runsRepository: RunsRepository by lazy {
        OfflineRunsRepository(HashireDatabase.getDatabase(context).runDao())
    }
}