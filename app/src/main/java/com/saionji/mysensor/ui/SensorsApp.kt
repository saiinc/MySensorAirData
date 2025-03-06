/*
 * Copyright © Anton Sorokin 2025. All rights reserved
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.saionji.mysensor.ui.screens.AboutScreen
import com.saionji.mysensor.ui.screens.HomeScreen
import com.saionji.mysensor.ui.screens.ShareScreen
import com.saionji.mysensor.ui.screens.saveBitmapToCache
import com.saionji.mysensor.ui.screens.shareUri

@Composable
fun SensorsApp(
    modifier: Modifier = Modifier
) {
    val mySensorViewModel: MySensorViewModel =
        viewModel(factory = MySensorViewModel.Factory)
    val optionsBoxState = mySensorViewModel.optionsBoxState
    val settingsApp = mySensorViewModel.settingsApp.value
    val settingsItems = mySensorViewModel.settingsItems

    val context = LocalContext.current

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
                        settingsApp = settingsApp,
                        onTextChange = {
                            mySensorViewModel.updateSensorIdTextState(newValue = it)
                        },
                        onAppSettingsChange = {
                            mySensorViewModel.updateSettingsAppState(newValue = it)
                        },
                        onSettingsChange = {
                            mySensorViewModel.updateSettingsItems(newValue = it)
                        },
                        onCloseClicked = {
                            mySensorViewModel.resetAppSettings()
                            mySensorViewModel.resetSettings()
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                        },
                        onDoneClicked = { mySettings, settingsApp ->
                            mySensorViewModel.saveAppSettings(settingsApp)
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
                        },
                        onShareClicked = { mySensorViewModel.setShowShareScreen(true) },
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
    if (mySensorViewModel.showShareScreen.value) {
        ShareScreen(
            settingsApp = mySensorViewModel.settingsApp.value,
            mySensorUiState = mySensorViewModel.mySensorUiState,
            onBitmapGenerated = { bitmap ->
                if (bitmap != null) {
                    saveBitmapToCache(context, bitmap)?.let { uri ->
                        shareUri(context, uri)
                    }
                }
                mySensorViewModel.setShowShareScreen(false) // Закрываем ShareScreen
            }
        )
    }
}
