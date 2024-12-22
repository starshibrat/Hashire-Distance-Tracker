package com.edu.hashire_distancetrackerapp.data

import kotlinx.coroutines.flow.Flow

interface RunsRepository {

    fun getAllRunsStream(): Flow<List<Run>>

    fun getRun(id: Int): Flow<Run>

    suspend fun insertRun(run: Run)

    suspend fun deleteRun(run: Run)

    suspend fun updateRun(run: Run)

}