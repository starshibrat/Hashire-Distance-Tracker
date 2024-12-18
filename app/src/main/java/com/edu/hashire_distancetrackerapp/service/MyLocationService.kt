package com.edu.hashire_distancetrackerapp.service

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.edu.hashire_distancetrackerapp.CHANNEL_ID
import com.edu.hashire_distancetrackerapp.R
import com.edu.hashire_distancetrackerapp.workers.LocServiceWorker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.UUID

class MyLocationService : Service() {
    private val priority = Priority.PRIORITY_HIGH_ACCURACY
    private val locationRequest by lazy {
        LocationRequest.Builder(priority, 1000).setIntervalMillis(3500).build()
    }
    private lateinit var fusedLocationProviderClient : FusedLocationProviderClient
    private lateinit var prevLocation : Location
    private var first = true

    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationAvailability(p0: LocationAvailability) {
                super.onLocationAvailability(p0)
            }

            override fun onLocationResult(location: LocationResult) {
                val acc = location.lastLocation?.accuracy
                log("location accuracy: $acc");

                startServiceOfForeground("Tracking the location.....\nAccuracy: $acc m")

                if (first) {
                    prevLocation = location.lastLocation!!
                } else {
                    log("prevLoc: $prevLocation")
                    log("curLoc: ${location.lastLocation}")

                    val distance = location.lastLocation?.distanceTo(prevLocation)
                    log("distance between 2 locations: $distance")

                    if (acc != null) {
                        if (distance!! < acc.toDouble()) {
                            return;
                        }
                    }

                }



                log("location accepted.")

                val lat = location.lastLocation?.latitude
                val long = location.lastLocation?.longitude

                val inputData = Data.Builder()
                    .putDouble("lat", lat!!)
                    .putDouble("long", long!!)
                    .build()

                val baseBluetoothUuidPostfix = "0000-1000-8000-00805F9B34FB"
                val uuid = UUID.fromString("0000FFF0-$baseBluetoothUuidPostfix")

                val workRequest = OneTimeWorkRequestBuilder<LocServiceWorker>()
                    .setInputData(inputData)
                    .setId(uuid)
                    .build()

                log("onLocationResult called")

                WorkManager.getInstance(this@MyLocationService).enqueueUniqueWork("UniqueTrackWorker",
                    ExistingWorkPolicy.REPLACE, workRequest)

                prevLocation = location.lastLocation!!
                first = false

//                startServiceOfForeground(lat.toString(), long.toString())

            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationUpdates()
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    override fun onDestroy() {
        super.onDestroy()
        log("service destroyed")
        WorkManager.getInstance(this).cancelUniqueWork("UniqueTrackWorker")
        stopLocationUpdates()
        stopForeground(true)
        stopSelf()
    }

    private fun log(str: String) {
        Log.d("MyLocationService", "log: $str ")
    }

    private fun locationUpdates() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null)

    }

    private fun startServiceOfForeground(content: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Location Tracking")
            .setContentText(content)
            .setOnlyAlertOnce(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                startForeground(1, notification)
            }
        } else {
            startForeground(1, notification)

        }



    }






}