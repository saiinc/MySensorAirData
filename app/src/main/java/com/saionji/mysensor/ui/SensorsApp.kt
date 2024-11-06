/*
 * Copyright © Anton Sorokin 2024. All rights reserved
 */

package com.saionji.mysensor.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.saionji.mysensor.ui.screens.AboutScreen
import com.saionji.mysensor.ui.screens.HomeScreen

@Composable
fun SensorsApp(
    modifier: Modifier = Modifier
) {
    val mySensorViewModel: MySensorViewModel =
        viewModel(factory = MySensorViewModel.Factory)
    val optionsBoxState = mySensorViewModel.optionsBoxState
    val settingsItems = mySensorViewModel.settingsItems

    val navController = rememberNavController()

    // Подписка на навигационные события
    LaunchedEffect(mySensorViewModel.navigationEvent) {
        mySensorViewModel.navigationEvent.collect { screen ->
            navController.navigate(screen)
        }
    }
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
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
                        onDoneClicked = { mySettings ->
                            mySensorViewModel.saveSettings(mySettings)
                            mySensorViewModel.getMySensor(mySettings)
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                        },
                        onOptionsBoxTriggered = {
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.OPENED)
                        },
                        onRefreshClicked = { mySettings ->
                            mySensorViewModel.getMySensor(mySettings)
                        },
                        onAboutClicked = {
                            mySensorViewModel.navigateTo("about")
                        }
                    )
                }
            ) {
                Surface(
                    modifier = Modifier
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
        composable("about") {
            AboutScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }

    }
}