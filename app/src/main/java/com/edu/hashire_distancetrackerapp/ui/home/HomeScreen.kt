package com.edu.hashire_distancetrackerapp.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
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
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),

){

    val locationPermissionsState = rememberMultiplePermissionsState(permissions =
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
        ) }
    )



    val allPermissionsRevoked = locationPermissionsState.permissions.size == locationPermissionsState.revokedPermissions.size

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold (
        topBar = {HashireTopAppBar(
            title = stringResource(id = HomeDestination.titleRes),
            canNavigateBack = false,
            scrollBehavior = scrollBehavior)},
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        if (locationPermissionsState.allPermissionsGranted || !allPermissionsRevoked) {
            HomeBody(
                run = viewModel.homeUiState,
                onRun = viewModel::startRun,
                onStop = viewModel::stopRun,
                location = viewModel.locationUiState,
                coordinates = viewModel.coordinates,
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
                    locationPermissionsState.launchMultiplePermissionRequest() }) {
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
    coordinates: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),

    ) {
    var btnText by remember {mutableStateOf("Start")}
    var isStarted by remember {
        mutableStateOf(false)
    }
    var isDone by remember {
        mutableStateOf(false)
    }

    var showRoute by remember {
        mutableStateOf(false)
    }

    val backgroundLocationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_BACKGROUND_LOCATION)

//    val internetPermissionState = rememberPermissionState(permission = Manifest.permission.INTERNET)
//    val accessNetworkPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_NETWORK_STATE)

    val mapPermissionState = rememberMultiplePermissionsState(permissions = listOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_NETWORK_STATE
    ))

    val ctx = LocalContext.current

    println(run.runDetails)
    
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(contentPadding)
    ) {
        Text(text = "Latitude: ${location.latitude}")
        Text(text = "Longitude: ${location.longitude}")
        Text(text = "Distance: ${run.runDetails.distance} km")

        Button(onClick = {

            if (isStarted) {
                btnText = "Start"
                isStarted = false
                onStop(ctx)
                isDone = true

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
        
        if (isDone) {
            Text(text = "=====RESULT=====")
            Text(text = "Speed: ${run.runDetails.speed} kph")
            Text(text = run.runDetails.time.milliseconds.toComponents {hours, minutes, seconds, _ ->"%02d:%02d:%02d".format(hours, minutes, seconds)})
            Button(onClick = {
                showRoute = !showRoute
            }) {
                if (!showRoute){
                    Text(text = "Show Route")
                } else {
                    Text(text = "Close Route")
                }
                

            }

            if (mapPermissionState.allPermissionsGranted && showRoute) {
                OpenMapDialog(coordinates = coordinates, onDismiss = {showRoute = !showRoute})
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
        Box (
            modifier = Modifier.fillMaxSize()
        ){
            MapBody(coordinates = coordinates)

            Row (
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
            factory = {
                    context ->
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
            update = {
                    view -> view.controller.setCenter(GeoPoint(coordinates[0].first, coordinates[0].second))
            }
        )

    }

    
}



@Composable
private fun RouteView(
    coordinates: List<Pair<Double, Double>>,
    lineColor: Color = Color.Blue,
    modifier: Modifier = Modifier
) {
    if (coordinates.isEmpty()) return

    val len = coordinates.size

    Canvas(modifier = modifier
        .fillMaxSize()
        .background(color = Color.LightGray)
        .padding(15.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val xMax = coordinates.maxBy { it.first }.first
        val xMin = coordinates.minBy { it.first }.first
        val yMax = coordinates.maxBy { it.second }.second
        val yMin = coordinates.minBy { it.second }.second

        var scaleX = 0.0
        var scaleY = 0.0

        if (xMax != xMin) {
            scaleX = canvasWidth / (xMax - xMin)
        }

        if (yMax != yMin) {
            scaleY = canvasHeight / (yMax - yMin)
        }


        val scaleFactor = min(scaleX, scaleY)

        val xOffset = (canvasWidth - (xMax - xMin) * scaleFactor)/2
        val yOffset = (canvasHeight - (yMax - yMin) * scaleFactor)/2

        Log.d("RouteView:", "MaxMin: $xMax $xMin $yMax $yMin")

        val pt = Path().apply{
            coordinates.forEachIndexed { index, it ->
                val x = (it.first - xMin) * scaleFactor + xOffset
                val y = (it.second - yMin) * scaleFactor + yOffset

                val xf = x.toFloat()
                val yf = y.toFloat()

                if (Pair(it.first, it.second) == Pair(coordinates[0].first, coordinates[0].second) || Pair(it.first, it.second) == Pair(coordinates[len - 1].first, coordinates[len - 1].second)) {
                    drawCircle(Color.Blue, radius = 5.dp.toPx(), center = Offset(x = xf, y= yf))

                }
                Log.d("RouteView:", "CoordinateDouble: ($x, $y)")
                Log.d("RouteView:", "CoordinateFloat: ($xf, $yf)")

                if (index == 0) {
                    moveTo(xf, yf)
                } else {
                    lineTo(xf, yf)
                }

            }
        }

//        pt.close()

        Log.d("RouteView: ", "drawing path...")
        drawPath(
            path = pt,
            color = Color.Blue,
            style = Stroke(3.2f),
        )

    }

}