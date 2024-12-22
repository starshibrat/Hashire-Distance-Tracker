package com.edu.hashire_distancetrackerapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(run: Run)

    @Update
    suspend fun update(run: Run)

    @Delete
    suspend fun delete(run: Run)

    @Query("SELECT * from runs WHERE id =:id")
    fun getRun(id: Int): Flow<Run>

    @Query("SELECT * from runs ORDER BY createdAt DESC")
    fun getAllRuns(): Flow<List<Run>>

}