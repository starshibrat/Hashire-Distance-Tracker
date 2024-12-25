package com.edu.hashire_distancetrackerapp

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier,
    title: String,
    canNavigateBack: Boolean,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateUp: () -> Unit = {},
    inHistoryPage: Boolean = false,
    navigateToHistory: () -> Unit = {},
) {

    CenterAlignedTopAppBar(
        modifier = modifier,
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
        },
        actions = {
            if (!inHistoryPage) {
                IconButton(
                    onClick = navigateToHistory) {
                    Icon(
                        imageVector = Icons.TwoTone.PlayArrow, contentDescription = "Run History")
                    
                }
            }
        }

        )

}