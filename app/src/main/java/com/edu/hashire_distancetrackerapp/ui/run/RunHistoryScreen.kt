package com.edu.hashire_distancetrackerapp.ui.run

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edu.hashire_distancetrackerapp.R
import com.edu.hashire_distancetrackerapp.ui.navigation.NavigationDestination
import androidx.lifecycle.viewmodel.compose.viewModel
import com.edu.hashire_distancetrackerapp.HashireTopAppBar
import com.edu.hashire_distancetrackerapp.data.Run
import com.edu.hashire_distancetrackerapp.ui.AppViewModelProvider


object RunHistoryDestination : NavigationDestination {
    override val route = "history"
    override val titleRes = R.string.history
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunHistoryScreen(
    navigateBack: () -> Unit,
    navigateToItemEntry: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RunHistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val historyUiState by viewModel.historyUiState.collectAsState()

    Scaffold (
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            HashireTopAppBar(
                modifier = modifier,
                title = stringResource(id = RunHistoryDestination.titleRes),
                canNavigateBack = true,
                scrollBehavior = scrollBehavior,
                navigateUp = navigateBack,
                inHistoryPage = true,
                )
        }
    ){

        innerPadding -> 
        RunHistoryBody(
            runList = historyUiState.runList,
            modifier = modifier.fillMaxSize(),
            onRunClick = navigateToItemEntry,
            contentPadding = innerPadding,
            )


    }


}

@Composable
private fun RunHistoryBody(
    runList: List<Run>,
    onRunClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {

        if (runList.isEmpty()) {
            Text(
                text = stringResource(id = R.string.no_run_history),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(contentPadding)
                )
        } else {
            RunList(
                runList = runList,
                onRunClick = {onRunClick(it.id)},
                contentPadding = contentPadding,
                modifier = Modifier.padding(
                    horizontal = 8.dp
                )
            )
        }

    }
}

@Composable
private fun RunList(
    runList: List<Run>,
    onRunClick: (Run) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {

    LazyColumn (
        modifier = modifier,
        contentPadding = contentPadding
    ){

        items(items = runList, key = {it.id}) {
            item -> RunItem(
                item = item,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { onRunClick(item) },

                )
        }

    }


}


@Composable
private fun RunItem(
    item: Run,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ){
            
            Row (
                modifier = Modifier.fillMaxWidth()
            ){
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.weight(1f))
                
            }
            Text(
                text = "Jarak: " + item.distance + " km",
                style = MaterialTheme.typography.titleMedium

            )
            Spacer(Modifier.weight(1f))
            
        }
        
    }
}