package com.edu.hashire_distancetrackerapp.workers

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.edu.hashire_distancetrackerapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val TAG = "LocationTrackerWorker"

class LocationTrackerWorker(var ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    companion object {
        const val Latitude = "Latitude"
        const val Longitude = "Longitude"
        const val Distance = "Distance"
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun doWork(): Result {

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ctx)
        val priority = Priority.PRIORITY_HIGH_ACCURACY

        Log.d(TAG, "doWork: WORK STARTED")
        var tempLoc: Location? = null
        for (i in 1..299) {
            delay(3000)
            var tempLong: Double = 0.0
            var tempLat: Double = 0.0
            var tempDist: Float = 0.0f

            Log.d(TAG, "doWork: WORK LOOP STARTED SIGMA")
            if (ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PERMISSION_GRANTED

            )  {

                break
            }


                fusedLocationProviderClient.getCurrentLocation(priority, CancellationTokenSource().token,
                ).addOnSuccessListener {
                    location: Location? ->
                    if (location == null) {
                        println("Failed to get current location")
                    } else {
                        GlobalScope.launch {

                            if (tempLoc == null) {
                                Log.d(TAG, "doWork: temploc null")
                                tempDist = 0.0f
                            } else {

//                                tempDist = location.distanceTo(tempLoc!!)
                                Log.d(TAG, "doWork: temploc not null, got distance: $tempDist")

                            }
                            tempLoc = location
                            tempLong = location.longitude
                            tempLat = location.latitude
                            setProgress(workDataOf(Longitude to tempLong, Latitude to tempLat))
                        }
                    }
                }

        }


        return Result.success()

    }



}