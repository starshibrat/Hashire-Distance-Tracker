package com.edu.hashire_distancetrackerapp.ui.run

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.hashire_distancetrackerapp.data.RunsRepository
import com.edu.hashire_distancetrackerapp.ui.home.RunDetails
import com.edu.hashire_distancetrackerapp.ui.home.RunUiState
import com.edu.hashire_distancetrackerapp.ui.home.toRun
import com.edu.hashire_distancetrackerapp.ui.home.toRunUiState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RunEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val runsRepository: RunsRepository
) : ViewModel() {

    var runUiState by mutableStateOf(RunUiState())

    private val runId: Int = checkNotNull(savedStateHandle[RunEditDestination.runIdArg])

    init {
        viewModelScope.launch {
            runUiState = runsRepository.getRun(runId)
                .filterNotNull()
                .first()
                .toRunUiState()
        }
    }

    fun updateUiState(runDetails: RunDetails) {
        runUiState = RunUiState(
            runDetails = runDetails,
            isEntryValid = validateInput(runDetails)
        )
    }

    private fun validateInput(uiState: RunDetails = runUiState.runDetails): Boolean {
        return with(uiState) {
            title.isNotBlank() && description.isNotBlank()
        }
    }

    suspend fun updateRun() {
        if (validateInput(runUiState.runDetails)) {
            runsRepository.updateRun(runUiState.runDetails.toRun())
        }
    }

}