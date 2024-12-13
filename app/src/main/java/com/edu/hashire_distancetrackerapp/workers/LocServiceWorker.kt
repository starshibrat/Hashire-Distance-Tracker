package com.edu.hashire_distancetrackerapp.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.edu.hashire_distancetrackerapp.workers.LocationTrackerWorker.Companion
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

const val TAG2 = "LocServiceWorker"

class LocServiceWorker(var ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    companion object {
        const val Latitude = "Latitude"
        const val Longitude = "Longitude"
    }


    override suspend fun doWork(): Result {


        val lat = inputData.getDouble("lat", 0.0)
        val long = inputData.getDouble("long", 0.0)

        Log.d(TAG2, "$lat ~ $long")

//        setProgress(workDataOf(Longitude to long, Latitude to lat))




        return Result.success(workDataOf(Longitude to long, Latitude to lat))
    }

}