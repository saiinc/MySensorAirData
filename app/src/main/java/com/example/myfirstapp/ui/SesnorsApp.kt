package com.example.myfirstapp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfirstapp.ui.screens.HomeScreen

@Composable
fun SensorsApp(
    modifier: Modifier = Modifier
) {
    val mySensorViewModel: MySensorViewModel =
        viewModel(factory = MySensorViewModel.Factory)
    val searchWidgetState = mySensorViewModel.optionsBoxState
    val historyItems = mySensorViewModel.historyItems

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainAppBar(
                searchWidgetState = searchWidgetState.value,
                historyItems = historyItems,
                onTextChange = {
                               mySensorViewModel.updateSensorIdTextState(newValue = it)
                },
                onCloseClicked = {
                    mySensorViewModel.resetSettings()
                    mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                },
                onSearchClicked = {
                    mySensorViewModel.saveSensorId(it)
                    mySensorViewModel.getMySensor(it.toMutableStateList())
                    mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                },
                onSearchTriggered = {
                    mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.OPENED)
                },
                onRefreshClicked = {
                    mySensorViewModel.getMySensor(it.toMutableStateList())
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
                retryAction = { mySensorViewModel.getMySensor(historyItems) },
                modifier = modifier
            )
        }
    }
}