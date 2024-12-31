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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
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

                    updateLocationUiState(LocationDetails(longitude = longValue, latitude = latValue))
                    Log.d("HomeViewModel", "startRun: change observed. Calculating the distance")
                    Log.d("HomeViewModel", "startRun: lat1: $latitude, long1: $longitude, lat2: $latValue, long2: $longValue")

                    dist = getDistance()

                    Log.d("HomeViewModel", "startRun: distance: $dist")

                    val run = RunDetails(
                        id = homeUiState.runDetails.id,
                        title = homeUiState.runDetails.title,
                        description = homeUiState.runDetails.description,
                        speed = homeUiState.runDetails.speed,
                        distance = dist
                    )

                    updateRunUiState(
                        run
                    )
                    latitude = latValue
                    longitude = longValue


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
            coordinates = coordinates
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
    
    private fun getDistance(coordinates: List<Pair<Double, Double>> = this.coordinates) : Double {

        if (coordinates.size == 1) {
            return 0.0
        }

        var c1 = Pair<Double, Double>(0.0, 0.0)
        var dist = 0.0
        var first = true

        for (coordinate in coordinates) {

            if (first) {
                c1 = coordinate
                first = false
                continue
            } else {
                dist += haversine(c1.first, c1.second, coordinate.first, coordinate.second)
                c1 = coordinate
            }
            
        }

        return dist
    }

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371
        val p1 = lat1 * Math.PI/180
        val p2 = lat2 * Math.PI/180
        val dp1 = (lat2 - lat1) * Math.PI/180
        val dl = (lon2 - lon1) * Math.PI/180

        val a = sin(dp1/2) * sin(dp1/2) + cos(p1) * cos(p1) * sin(dl/2) * sin(dl/2)

        val c = 2 * atan2(sqrt(a), sqrt(1-a))

        return R*c


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
    val createdAt: Date = Date(),
    val coordinates: List<Pair<Double, Double>> = listOf()
)

fun RunDetails.toRun(): Run = Run(
    id = id,
    title = title,
    distance = distance,
    description = description,
    speed = speed,
    time = time,
    createdAt = createdAt,
    coordinates = coordinates
)

fun Run.toRunDetails(): RunDetails = RunDetails(
    id = id,
    title = title,
    distance = distance,
    description = description,
    speed = speed,
    time = time,
    createdAt = createdAt,
    coordinates = coordinates,
)

fun Run.toRunUiState(): RunUiState = RunUiState(
    runDetails = this.toRunDetails()
)

