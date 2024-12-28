package com.edu.hashire_distancetrackerapp.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.edu.hashire_distancetrackerapp.data.Run
import com.edu.hashire_distancetrackerapp.data.RunsRepository
import com.edu.hashire_distancetrackerapp.service.MyLocationService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt

class HomeViewModel(
    private val application: Application,
    private val runsRepository: RunsRepository,
) : ViewModel(){
    var homeUiState by mutableStateOf(RunUiState())
        private set

    var locationUiState by mutableStateOf(LocationDetails())
        private set

    var coordinates = mutableStateListOf<Pair<Double, Double>>()
    private var startTime = System.currentTimeMillis()

    private var workerProgress: LiveData<WorkInfo> = MutableLiveData()
    private val intent = Intent(application.applicationContext, MyLocationService::class.java)
    private var index = 0
    fun updateRunUiState(runDetails: RunDetails) {
        homeUiState = RunUiState(
            runDetails = runDetails
        )
    }

    private fun updateLocationUiState(locationDetails: LocationDetails) {
        locationUiState = locationDetails
    }

    init {
        viewModelScope.launch {
            homeUiState = Run().toRunUiState()
            locationUiState = LocationDetails()
        }
    }

    fun startRun(ctx: Context) {
        coordinates.clear()
        var latitude = 0.0
        var longitude = 0.0
        updateRunUiState(runDetails = RunDetails())
        updateLocationUiState(locationDetails = LocationDetails())

        var first = true
        startTime = System.currentTimeMillis()

        application.startService(intent)

        val baseBluetoothUuidPostfix = "0000-1000-8000-00805F9B34FB"
        val uuid = UUID.fromString("0000FFF0-$baseBluetoothUuidPostfix")

        workerProgress = WorkManager.getInstance(ctx).getWorkInfoByIdLiveData(uuid)

        workerProgress.observeForever {
                workInfo: WorkInfo? ->
            if (workInfo != null) {

                if (workInfo.state.isFinished) {

                    val progress = workInfo.outputData

                    val longValue = progress.getDouble("Longitude", 0.0)
                    val latValue = progress.getDouble("Latitude", 0.0)
                    var dist = 0.0
                    Log.d("HomeViewModel", "startRun: index: $index")
                    if (coordinates.isEmpty() || coordinates[index - 1] != Pair(longValue, latValue)) {
                        coordinates.add(Pair(latValue, longValue))
                        index += 1
                    }

                    Log.d("HomeViewModel", "startRun: longValue: $longValue, latValue: $latValue")

                    if (first) {

                        latitude = latValue
                        longitude = longValue
                        first = false

                    }

                    updateLocationUiState(LocationDetails(longitude = longValue, latitude = latValue))
                    Log.d("HomeViewModel", "startRun: change observed. Calculating the distance")
                    Log.d("HomeViewModel", "startRun: lat1: $latitude, long1: $longitude, lat2: $latValue, long2: $longValue")

                    dist = if (latitude != 0.0 && longitude != 0.0 && latValue != 0.0 && longValue != 0.0) {
                        getDistanceInKm(latitude, longitude, latValue, longValue)

                    } else {
                        0.0
                    }


                    Log.d("HomeViewModel", "startRun: distance: $dist")

                    val run = RunDetails(
                        id = homeUiState.runDetails.id,
                        title = homeUiState.runDetails.title,
                        description = homeUiState.runDetails.description,
                        speed = homeUiState.runDetails.speed,
                        distance = homeUiState.runDetails.distance + dist
                    )

                    updateRunUiState(
                        run
                    )


                }

            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun stopRun(ctx: Context) {
        WorkManager.getInstance(ctx).cancelAllWork()
        application.stopService(intent)
        index = 0

        val diffTime = System.currentTimeMillis() - startTime
        val speed = this.homeUiState.runDetails.distance / diffTime * 3_600_000

        val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC))
        } else {
            Date()
        }

        val run = RunDetails(
            id = homeUiState.runDetails.id,
            title = "run ${SimpleDateFormat("dd-MM-yyyy").format(now)}",
            description = homeUiState.runDetails.description,
            speed = speed,
            distance = homeUiState.runDetails.distance,
            time = diffTime,
            createdAt = now,
        )
        Log.d("HomeViewModel", "stopRun: diffTime: $diffTime")
        Log.d("HomeViewModel", "stopRun: time: $now")

        updateRunUiState(runDetails = run)


        Log.d("HomeViewModel", "stopRun: coordinates: $coordinates")

    }

    private fun getDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double) : Double {

        val radius = 6371
        val p = PI / 180

        val a = 0.5 - cos((lat2-lat1) * p) / 2 + cos(lat1 * p) * cos(lat2 * p) * (1 - cos((lon2 - lon1) * p)) / 2

        return 2 * radius * asin(sqrt(a))


    }

    suspend fun saveRun() {
        runsRepository.insertRun(homeUiState.runDetails.toRun())
    }

}

data class RunUiState(
    val runDetails: RunDetails = RunDetails(),
    val isEntryValid: Boolean = false
)

data class LocationDetails(
    val longitude: Double? = 0.0,
    val latitude: Double? = 0.0
)

data class RunDetails(
    val id: Int = 0,
    val title: String = "",
    val distance: Double = 0.0,
    val description: String = "",
    val speed: Double = 0.0,
    val time: Long = 0,
    val createdAt: Date = Date()
)

fun RunDetails.toRun(): Run = Run(
    id = id,
    title = title,
    distance = distance,
    description = description,
    speed = speed,
    time = time,
    createdAt = createdAt
)

fun Run.toRunDetails(): RunDetails = RunDetails(
    id = id,
    title = title,
    distance = distance,
    description = description,
    speed = speed,
    time = time,
    createdAt = createdAt
)

fun Run.toRunUiState(): RunUiState = RunUiState(
    runDetails = this.toRunDetails()
)

