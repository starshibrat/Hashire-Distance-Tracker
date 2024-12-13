package com.edu.hashire_distancetrackerapp.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.edu.hashire_distancetrackerapp.HashireApplication
import com.edu.hashire_distancetrackerapp.ui.home.HomeViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                hashireApplication()
            )
        }
    }
}

fun CreationExtras.hashireApplication(): HashireApplication = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HashireApplication)