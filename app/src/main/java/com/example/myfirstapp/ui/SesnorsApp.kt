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
    val optionsBoxState = mySensorViewModel.optionsBoxState
    val settingsItems = mySensorViewModel.settingsItems

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            MainAppBar(
                optionsBoxState = optionsBoxState.value,
                settingsItems = settingsItems,
                onTextChange = {
                               mySensorViewModel.updateSensorIdTextState(newValue = it)
                },
                onSettingsChange = {
                                   mySensorViewModel.updateSettingsItems(newValue = it)
                },
                onCloseClicked = {
                    mySensorViewModel.resetSettings()
                    mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                },
                onDoneClicked = {
                    mySensorViewModel.saveSensorId(it)
                    mySensorViewModel.getMySensor(it)
                    mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                },
                onOptionsBoxTriggered = {
                    mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.OPENED)
                },
                onRefreshClicked = {
                    mySensorViewModel.getMySensor(it)
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
                retryAction = { mySensorViewModel.getMySensor(settingsItems) },
                modifier = modifier
            )
        }
    }
}