package com.edu.hashire_distancetrackerapp.data

import kotlinx.coroutines.flow.Flow

class OfflineRunsRepository(private val runDao: RunDao) : RunsRepository {
    override fun getAllRunsStream(): Flow<List<Run>> {
        return runDao.getAllRuns()
    }

    override fun getRun(id: Int): Flow<Run> {
        return runDao.getRun(id)
    }

    override suspend fun insertRun(run: Run) {
        return runDao.insert(run)
    }

    override suspend fun deleteRun(run: Run) {
        return runDao.delete(run)
    }

    override suspend fun updateRun(run: Run) {
        return runDao.update(run)
    }


}