package com.edu.hashire_distancetrackerapp.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermission() {
    val locationPermissionsState = rememberMultiplePermissionsState(permissions = listOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    if (locationPermissionsState.allPermissionsGranted) {
        Text(text = "Thanks! I can access your exact location :D")
    } else {
        Column {
            val allPermissionsRevoked = locationPermissionsState.permissions.size == locationPermissionsState.revokedPermissions.size

            val textToShow = if (!allPermissionsRevoked) {
            "Yay! Thanks for letting me access your approximate location. " +
                    "But you know what would be great? If you allow me to know where you " +
                    "exactly are. Thank you!"
            } else if (locationPermissionsState.shouldShowRationale) {
                "Getting your exact location is important for this app. " +
                        "Please grant us fine location. Thank you :D"
            } else {
                "This feature requires location permission"
            }

            val buttonText = if (!allPermissionsRevoked) {
                "Allow precise location"
            } else {
                "Request permissions"
            }

            Text(text = textToShow)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { locationPermissionsState.launchMultiplePermissionRequest() }) {
                Text(buttonText)
            }

        }
    }

}