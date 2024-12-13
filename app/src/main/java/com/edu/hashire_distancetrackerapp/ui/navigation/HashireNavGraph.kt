package com.edu.hashire_distancetrackerapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.edu.hashire_distancetrackerapp.ui.home.HomeDestination
import com.edu.hashire_distancetrackerapp.ui.home.HomeScreen

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

            )
        }

    }
}