package com.edu.hashire_distancetrackerapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.edu.hashire_distancetrackerapp.ui.home.HomeDestination
import com.edu.hashire_distancetrackerapp.ui.home.HomeScreen
import com.edu.hashire_distancetrackerapp.ui.run.RunHistoryDestination
import com.edu.hashire_distancetrackerapp.ui.run.RunHistoryScreen

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun HashireNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        modifier = modifier
        ) {

        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToHistory = {
                    navController.navigate(RunHistoryDestination.route)
                }
            )
        }
        composable(route = RunHistoryDestination.route) {
            RunHistoryScreen(
                navigateBack = {
                navController.popBackStack()
                },

                )
        }

    }
}