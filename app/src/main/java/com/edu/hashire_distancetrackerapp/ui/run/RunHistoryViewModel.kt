package com.edu.hashire_distancetrackerapp.ui.run

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edu.hashire_distancetrackerapp.data.Run
import com.edu.hashire_distancetrackerapp.data.RunsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


class RunHistoryViewModel(runsRepository: RunsRepository) : ViewModel() {
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    val historyUiState: StateFlow<HistoryUiState> = runsRepository.getAllRunsStream().map {
        HistoryUiState(it)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = HistoryUiState()
        )

}

data class HistoryUiState(val runList: List<Run> = listOf())