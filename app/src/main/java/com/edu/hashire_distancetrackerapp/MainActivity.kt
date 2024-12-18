package com.edu.hashire_distancetrackerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.WorkManager
import com.edu.hashire_distancetrackerapp.service.MyLocationService
import com.edu.hashire_distancetrackerapp.ui.home.HomeViewModel
import com.edu.hashire_distancetrackerapp.ui.theme.HashireDistanceTrackerAppTheme
import org.osmdroid.library.BuildConfig
import java.io.File

const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val osmConf = org.osmdroid.config.Configuration.getInstance()
        val basePath = File(cacheDir.absolutePath, "osmdroid")
        osmConf.osmdroidBasePath = basePath
        val tileCache = File(osmConf.osmdroidBasePath.absolutePath, "tile")
        osmConf.osmdroidTileCache = tileCache
        osmConf.userAgentValue = BuildConfig.LIBRARY_PACKAGE_NAME
        enableEdgeToEdge()

        setContent {
            HashireDistanceTrackerAppTheme {
                Surface (
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {

                    HashireApp() }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        WorkManager.getInstance(this).cancelAllWork()
        val intent = Intent(this, MyLocationService::class.java)
        stopService(intent)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: started")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: started")
    }

}

