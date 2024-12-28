package com.edu.hashire_distancetrackerapp.ui.run

import android.annotation.SuppressLint
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edu.hashire_distancetrackerapp.R
import com.edu.hashire_distancetrackerapp.ui.navigation.NavigationDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edu.hashire_distancetrackerapp.HashireTopAppBar
import com.edu.hashire_distancetrackerapp.data.Run
import com.edu.hashire_distancetrackerapp.ui.AppViewModelProvider
import com.edu.hashire_distancetrackerapp.ui.home.OpenMapDialog
import com.edu.hashire_distancetrackerapp.ui.home.RunDetails
import com.edu.hashire_distancetrackerapp.ui.home.toRun
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


object RunDetailsDestination : NavigationDestination {
    override val route = "run_details"
    override val titleRes = R.string.run_detail_title
    const val runIdArg = "runId"
    val routeWithArgs = "$route/{$runIdArg}"

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunDetailsScreen(
    navigateToEditRun: (Int) -> Unit,
    navigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RunDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    val uiState = viewModel.runDetailsUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            HashireTopAppBar(
                title = stringResource(id = RunDetailsDestination.titleRes),
                canNavigateBack = true,
                navigateUp = navigateBack,
                inHistoryPage = true,
                modifier = Modifier
                )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navigateToEditRun(uiState.value.runDetails.id)
                },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(20.dp)
                ) {

                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Run")

            }
        },
        modifier = modifier
    ) {
        innerPadding ->
        RunDetailsBody(
            runDetailsUiState = uiState.value,
            onDelete = {
                coroutineScope.launch {
                    viewModel.deleteRun()
                    navigateBack()
                }
            },
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding()
                )
                .verticalScroll(rememberScrollState())
            )


    }

}

@Composable
private fun RunDetailsBody(
    runDetailsUiState: RunDetailsUiState,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        var deleteConfirmationRequired by rememberSaveable {
            mutableStateOf(false)
        }

        var showRoute by rememberSaveable {
            mutableStateOf(false)
        }

        RunDetails(
            run = runDetailsUiState.runDetails.toRun(),
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            showRoute = !showRoute
        }) {
            Text(text = "Show Route")
        }

        if (showRoute) {
            OpenMapDialog(
                coordinates = runDetailsUiState.runDetails.coordinates,
                onDismiss = {
                    showRoute = !showRoute
                })
        }

        OutlinedButton(onClick = {
            deleteConfirmationRequired = true
        }) {

            Text(text = "Delete")

        }

        if (deleteConfirmationRequired) {
            DeleteConfirmationDialog(
                onDeleteConfirm = {

                    deleteConfirmationRequired = false
                    onDelete()
            },
                onDeleteCancel = {
                    deleteConfirmationRequired = false
            },
                modifier = Modifier.padding(16.dp)
            )
        }

    }

}

@SuppressLint("DefaultLocale")
@Composable
fun RunDetails(
    run: Run,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            RunDetailsColumn(
                labelResID = R.string.run_title, runDetail = run.title,
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )

            RunDetailsColumn(
                labelResID = R.string.run_distance, runDetail = "${String.format("%.2f", run.distance)} km",
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )

            RunDetailsColumn(
                labelResID = R.string.run_time, runDetail = run.time.milliseconds.toComponents { hours, minutes, seconds, _ ->
                    "%02d:%02d:%02d".format(
                        hours,
                        minutes,
                        seconds
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )

            RunDetailsColumn(
                labelResID = R.string.run_speed, runDetail = "${String.format("%.2f", run.speed)} km/h",
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )

            RunDetailsColumn(
                labelResID = R.string.run_description,
                runDetail = if (run.description.isNotBlank()) run.description else "-",
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )

            RunDetailsColumn(
                labelResID = R.string.run_date, runDetail = run.createdAt.toString(),
                modifier = Modifier.padding(
                    horizontal = 16.dp
                )
            )


        }

    }

}

@Composable
private fun RunDetailsColumn(
    @StringRes labelResID: Int,
    runDetail: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {

        Text(stringResource(id = labelResID))
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = runDetail,
            fontWeight = FontWeight.Bold
            )

    }

}

@Composable
private fun DeleteConfirmationDialog(
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { /*TODO*/ },
        confirmButton = {
            TextButton(
                onClick = onDeleteConfirm
            ) {

                Text(text="Yes")

            }
        },
        title = {
            Text(text = "Attention")
        },
        text = {
            Text(text = "Are you sure you want to delete?")
        },
        modifier = modifier,
        dismissButton = {
            TextButton(onClick = onDeleteCancel) {
                
                Text(text = "No")
                
            }
        }
    )
}