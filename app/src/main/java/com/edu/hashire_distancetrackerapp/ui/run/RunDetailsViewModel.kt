package com.edu.hashire_distancetrackerapp.ui.run

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.hashire_distancetrackerapp.data.RunsRepository
import com.edu.hashire_distancetrackerapp.ui.home.RunDetails
import com.edu.hashire_distancetrackerapp.ui.home.toRun
import com.edu.hashire_distancetrackerapp.ui.home.toRunDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RunDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val runsRepository: RunsRepository
) : ViewModel() {

    private val runId: Int = checkNotNull(savedStateHandle[RunDetailsDestination.runIdArg])

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    suspend fun deleteRun() {
        runsRepository.deleteRun(runDetailsUiState.value.runDetails.toRun())
    }

    val runDetailsUiState: StateFlow<RunDetailsUiState> = runsRepository.getRun(runId)
        .filterNotNull()
        .map{
            RunDetailsUiState(runDetails = it.toRunDetails())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = RunDetailsUiState()
        )

}

data class RunDetailsUiState(
    val runDetails: RunDetails = RunDetails()
)