package com.example.myfirstapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfirstapp.ui.screens.HomeScreen

@Composable
fun SensorsApp(
    modifier: Modifier = Modifier
) {
    val mySensorViewModel: MySensorViewModel =
        viewModel(factory = MySensorViewModel.Factory)
    val searchWidgetState = mySensorViewModel.searchWidgetState
    val searchTextState = mySensorViewModel.searchTextState
    var historyItems = mySensorViewModel.historyItems

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainAppBar(
                searchWidgetState = searchWidgetState.value,
                searchTextState = searchTextState.value,
                historyItems = historyItems,
                onTextChange = {
                               mySensorViewModel.updateSearchTextState(newValue = it)
                },
                onCloseClicked = {
                    mySensorViewModel.updateSearchWidgetState(newValue = SearchWidgetState.CLOSED)
                },
                onSearchClicked = {
                    mySensorViewModel.saveSensorId(it)
                    mySensorViewModel.getMySensor(it)
                    mySensorViewModel.updateSearchWidgetState(newValue = SearchWidgetState.CLOSED)
                },
                onSearchTriggered = {
                    historyItems = mySensorViewModel.historyItems
                    mySensorViewModel.updateSearchWidgetState(newValue = SearchWidgetState.OPENED)
                }
            )
        }
    ) {
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            HomeScreen(
                mySensorUiState = mySensorViewModel.mySensorUiState,
                retryAction = { mySensorViewModel.getMySensor(mySensorViewModel.getSensorId()) },
                modifier = modifier
            )
        }
    }
}