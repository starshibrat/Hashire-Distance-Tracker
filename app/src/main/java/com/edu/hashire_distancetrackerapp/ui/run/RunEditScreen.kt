package com.edu.hashire_distancetrackerapp.ui.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.edu.hashire_distancetrackerapp.R
import com.edu.hashire_distancetrackerapp.ui.navigation.NavigationDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edu.hashire_distancetrackerapp.HashireTopAppBar
import com.edu.hashire_distancetrackerapp.ui.AppViewModelProvider
import com.edu.hashire_distancetrackerapp.ui.home.RunDetails
import com.edu.hashire_distancetrackerapp.ui.home.RunUiState
import kotlinx.coroutines.launch


object RunEditDestination : NavigationDestination {
    override val route = "run_edit"
    override val titleRes = R.string.run_edit
    const val runIdArg = "runId"
    val routeWithArgs = "$route/{$runIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunEditScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RunEditViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            HashireTopAppBar(
                modifier = modifier,
                title = stringResource(id = RunEditDestination.titleRes),
                canNavigateBack = true,
                inHistoryPage = true,
                navigateUp = onNavigateUp)
        },
        modifier = modifier
    ) {
        innerPadding ->
        RunEditBody(
            runUiState = viewModel.runUiState,
            onRunValueChange = viewModel::updateUiState,
            onSaveClick = {
                coroutineScope.launch {
                    viewModel.updateRun()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding()
                )
        )


    }

}

@Composable
private fun RunEditBody(
    runUiState: RunUiState,
    onRunValueChange: (RunDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column (
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = modifier.padding(16.dp)
    ) {

        RunEditForm(
            runDetails = runUiState.runDetails,
            onValueChange = onRunValueChange,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveClick,
            enabled = runUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            
            Text(text = "Save")

        }

    }

}

@Composable
private fun RunEditForm(
    runDetails: RunDetails,
    modifier: Modifier = Modifier,
    onValueChange: (RunDetails) -> Unit = {},
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        OutlinedTextField(
            value = runDetails.title,
            onValueChange = {
                onValueChange(runDetails.copy(title = it))
            },
            label = { Text(text = stringResource(id = R.string.run_detail_title)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true
        )

        OutlinedTextField(
            value = runDetails.description,
            onValueChange = {
                onValueChange(runDetails.copy(description = it))
            },
            label = { Text(text = stringResource(id = R.string.run_description)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            minLines = 5
        )

        if (enabled) {
            Text(
                text = "*required fields",
                modifier = Modifier.padding(start = 16.dp)
            )

        }

    }
}