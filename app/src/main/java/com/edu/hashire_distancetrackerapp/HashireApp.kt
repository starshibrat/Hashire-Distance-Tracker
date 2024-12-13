package com.edu.hashire_distancetrackerapp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.edu.hashire_distancetrackerapp.ui.navigation.HashireNavHost

@Composable
fun HashireApp(
    navController: NavHostController = rememberNavController()
) {

    HashireNavHost(navController = navController)

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashireTopAppBar(
    title: String,
    canNavigateBack: Boolean,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {}
) {

    CenterAlignedTopAppBar(
        title = { Text(text = title)},
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = navigateUp
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go Back")

                }
            }
        }
        )

}