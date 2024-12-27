package com.edu.hashire_distancetrackerapp.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.FlowColumnScopeInstance.align
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.edu.hashire_distancetrackerapp.R
import com.edu.hashire_distancetrackerapp.ui.navigation.NavigationDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edu.hashire_distancetrackerapp.HashireTopAppBar
import com.edu.hashire_distancetrackerapp.ui.AppViewModelProvider
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.text.SimpleDateFormat
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object HomeDestination : NavigationDestination {
    override val route = "home"
    override val titleRes = R.string.app_name
}

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToHistory: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),

    ) {


    val coroutineScope = rememberCoroutineScope()

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
    )


    val allPermissionsRevoked =
        locationPermissionsState.permissions.size == locationPermissionsState.revokedPermissions.size

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            HashireTopAppBar(
                modifier = modifier.background(color = Color.LightGray),
                title = stringResource(id = HomeDestination.titleRes),
                canNavigateBack = false,
                scrollBehavior = scrollBehavior,
                navigateToHistory = navigateToHistory
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        if (locationPermissionsState.allPermissionsGranted || !allPermissionsRevoked) {
            HomeBody(
                run = viewModel.homeUiState,
                onRun = viewModel::startRun,
                onStop = viewModel::stopRun,
                location = viewModel.locationUiState,
                coordinates = viewModel.coordinates,
                onSave = {
                    coroutineScope.launch {
                        viewModel.saveRun()
                    }
                },
                contentPadding = innerPadding,
            )

        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.height(150.dp))
                Text(text = "Allow all permissions to use the application service.")
                Button(onClick = {
                    locationPermissionsState.launchMultiplePermissionRequest()
                }) {
                    Text("Request Permissions")
                }
            }
        }
    }


}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun HomeBody(
    run: RunUiState,
    location: LocationDetails,
    onRun: (Context) -> Unit,
    onStop: (Context) -> Unit,
    onSave: () -> Unit,
    coordinates: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),

    ) {
    var btnText by remember { mutableStateOf("Start") }
    var isStarted by remember {
        mutableStateOf(false)
    }
    var isDone by remember {
        mutableStateOf(false)
    }

    var showRoute by remember {
        mutableStateOf(false)
    }

    val backgroundLocationPermissionState =
        rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)

//    val internetPermissionState = rememberPermissionState(permission = Manifest.permission.INTERNET)
//    val accessNetworkPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_NETWORK_STATE)

    val mapPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
    )

    val ctx = LocalContext.current

    println(run.runDetails)

    Box(
        modifier = modifier
            .fillMaxSize()
//            .padding(12.dp, 30.dp, 12.dp, 12.dp)
//            .background(shape = RoundedCornerShape(10.dp))
    ) {
        Box (
            modifier = modifier.padding(20.dp)
//            modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color.Gray).padding(12.dp)
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .padding(contentPadding)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)

            ) {
                Box (
                    modifier = modifier
                        .background(Color.Gray)
                        .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 12.dp)
                ){
                    Column (
                        horizontalAlignment = Alignment.CenterHorizontally
                    ){
                        Row(
                            modifier = modifier
                                .fillMaxWidth(),
                            Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Latitude:",
                            )
                            Text(
                                "${location.latitude}",
                            )
                        }
                        Row (
                            modifier = modifier.fillMaxWidth(),
                            Arrangement.SpaceBetween
                        ){
                            Text(text = "Longitude: ")
                            Text("${location.longitude}")
                        }
                        Row (
                            modifier.fillMaxWidth(),
                            Arrangement.SpaceBetween
                        ){
                            Text(text = "Distance: ")
                            Text("${run.runDetails.distance} Km")
                        }

                        Button(onClick = {
                            if (isStarted) {
                                btnText = "Start"
                                isStarted = false
                                onStop(ctx)
                                isDone = true
                                onSave()

                            } else {
                                btnText = "Stop"
                                isStarted = true
                                isDone = false

                                if (!backgroundLocationPermissionState.status.isGranted) {
                                    backgroundLocationPermissionState.launchPermissionRequest()
                                }

                                onRun(ctx)

                            }

                        }
                        ) {
                            Text(text = btnText)
                        }
                    }
                }

                if (isDone) {
                    Box (
                        modifier
                            .background(Color.DarkGray)
                            .fillMaxWidth()
                            .padding(15.dp),
                        Alignment.Center
                    ){
                        Text(text = "RESULT",style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 25.sp,), color = Color.White)
                    }
                    Box (
                        modifier.padding(12.dp)
                    ){
                        Column {
                            Row(
                                modifier.fillMaxWidth(),
                                Arrangement.SpaceBetween
                            ) {
                                Text(text = "Speed: ")
                                Text("${run.runDetails.speed} km/h")
                            }
                            Row(
                                modifier.fillMaxWidth(),
                                Arrangement.SpaceBetween
                            ) {
                                Text("Time :")
                                Text(text = run.runDetails.time.milliseconds.toComponents { hours, minutes, seconds, _ ->
                                    "%02d:%02d:%02d".format(
                                        hours,
                                        minutes,
                                        seconds
                                    )
                                })
                            }
                        }
                    }
                    Box (
                        modifier.padding(bottom = 12.dp)
                    ){
                        Button(onClick = {
                            showRoute = !showRoute
                        }) {
                            if (!showRoute) {
                                Text(text = "Show Route")
                            } else {
                                Text(text = "Close Route")
                            }


                        }
                    }

                    if (mapPermissionState.allPermissionsGranted && showRoute) {
                        OpenMapDialog(
                            coordinates = coordinates,
                            onDismiss = { showRoute = !showRoute })
                    }

                }

            }
        }
    }

}

val dummyCoordinates = listOf(
    Pair(38.8986, -77.0365), // Point 1
    Pair(38.8985, -77.0359), // Point 2
    Pair(38.8983, -77.0354), // Point 3
    Pair(38.8980, -77.0349), // Point 4
    Pair(38.8977, -77.0345), // Point 5
    Pair(38.8973, -77.0343), // Point 6
    Pair(38.8969, -77.0345), // Point 7
    Pair(38.8965, -77.0349), // Point 8
    Pair(38.8962, -77.0354), // Point 9
    Pair(38.8960, -77.0359), // Point 10
    Pair(38.8960, -77.0365), // Point 11
    Pair(38.8962, -77.0370), // Point 12
    Pair(38.8965, -77.0375), // Point 13
    Pair(38.8969, -77.0378), // Point 14
    Pair(38.8973, -77.0380), // Point 15
    Pair(38.8977, -77.0382), // Point 16
    Pair(38.8980, -77.0380), // Point 17
    Pair(38.8983, -77.0378), // Point 18
    Pair(38.8985, -77.0375), // Point 19
    Pair(38.8986, -77.0370)  // Point 20 (Back near the start)

)

@Composable
private fun OpenMapDialog(
    coordinates: List<Pair<Double, Double>>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            MapBody(coordinates = coordinates)

            Row(
                horizontalArrangement = Arrangement.End
            )
            {
                Button(onClick = onDismiss) {
                    Text(text = "Close")

                }
                Text(text = "© OpenStreetMap contributors")
            }


        }

    }
}

@Composable
private fun MapBody(coordinates: List<Pair<Double, Double>> = dummyCoordinates) {
    Column {
        AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setZoomLevel(19.1)
                    setOnClickListener {

                    }

                    coordinates.forEach {
                        val point = GeoPoint(it.first, it.second)
                        val marker = Marker(this)

                        marker.position = point
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = "(${point.latitude}, ${point.longitude})"
                        overlays.add(marker)


                    }

                    invalidate()
                }
            },
            update = { view ->
                view.controller.setCenter(GeoPoint(coordinates[0].first, coordinates[0].second))
            }
        )

    }


}