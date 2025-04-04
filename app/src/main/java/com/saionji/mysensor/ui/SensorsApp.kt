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
import androidx.compose.runtime.collectAsState
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
    val settingsItems = mySensorViewModel.settingsItems.collectAsState()
    val sensorsOptions = mySensorViewModel.sensorsOptions.collectAsState()
    val isRefreshing = mySensorViewModel.isRefreshing.collectAsState().value

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
                        sensorsOptions = sensorsOptions,
                        settingsApp = settingsApp,
                        onTextChange = {
                            mySensorViewModel.updateSensorIdTextState(newValue = it)
                        },
                        onAppSettingsChange = {
                            mySensorViewModel.updateSettingsAppState(newValue = it)
                        },
                        onCloseClicked = {
                            mySensorViewModel.resetAppSettings()
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                        },
                        onDoneClicked = { sensorsOptions, settingsApp ->
                            mySensorViewModel.saveAppSettings(settingsApp)
                            val updatedSensors = sensorsOptions.value.map { it.copy() }
                            mySensorViewModel.saveSensors(updatedSensors)
                            mySensorViewModel.sensorsLoad(updatedSensors)
                            mySensorViewModel.getDeviceSensors()
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.CLOSED)
                        },
                        onAddClicked = {
                            mySensorViewModel.addSensorOptions()
                        },
                        onRemoveClicked = {
                            mySensorViewModel.removeSensorOptions(it)
                        },
                        onEditSensorId = { index, settingsSensorItemId ->
                            mySensorViewModel.updateSensorOptionsItemId(index = index, settingsSensorItemId = settingsSensorItemId)
                        },
                        onEditSensorDescription = { index, settingsSensorItemDescription->
                            mySensorViewModel.updateSensorOptionsItemDescription(index = index, settingsSensorItemDescription = settingsSensorItemDescription)
                        },
                        onOptionsBoxTriggered = {
                            mySensorViewModel.sensorsOptionsLoad()
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.OPENED)
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
                        settingsItems = settingsItems,
                        isRefreshing = isRefreshing,
                        onRefresh = mySensorViewModel::refresh
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
            settingsItems = settingsItems,
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
