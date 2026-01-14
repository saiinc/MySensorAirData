/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ViewComfy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.saionji.mysensor.R
import com.saionji.mysensor.ui.map.MapViewModel
import com.saionji.mysensor.ui.screens.AboutScreen
import com.saionji.mysensor.ui.screens.HomeScreen
//import com.saionji.mysensor.ui.screens.MapScreen
import com.saionji.mysensor.ui.map.MapScreen
import com.saionji.mysensor.ui.screens.ShareScreen
import com.saionji.mysensor.ui.screens.saveBitmapToCache
import com.saionji.mysensor.ui.screens.shareUri
import kotlinx.coroutines.delay
import org.maplibre.android.geometry.LatLng

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SensorsApp(
    modifier: Modifier = Modifier
) {
    val mySensorViewModel: MySensorViewModel =
        viewModel(factory = MySensorViewModel.Factory)
    val mapViewModel: MapViewModel =
        viewModel(factory = MapViewModel.Factory)
    val optionsBoxState = mySensorViewModel.optionsBoxState
    val settingsApp = mySensorViewModel.settingsApp.value
    val settingsItems = mySensorViewModel.settingsItems.collectAsState()
    val sensorsOptions = mySensorViewModel.sensorsOptions.collectAsState()
    val isRefreshing = mySensorViewModel.isRefreshing.collectAsState().value
    val showError by mySensorViewModel.showErrorMessage.collectAsState()
    val currentScreen by mySensorViewModel.currentScreen.collectAsState()
    val currentLocation: LatLng? = null
    val backgroundColor = MaterialTheme.colorScheme.surface.toArgb()
    val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val strokeColor = MaterialTheme.colorScheme.outline.toArgb()
    val errorMessage = stringResource(R.string.error_loading)

    val context = LocalContext.current

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    val allPermissionsGranted = locationPermissionsState.permissions.all { it.status.isGranted }

    val navController = rememberNavController()

    // Подписка на навигационные события
    LaunchedEffect(mySensorViewModel.navigationEvent) {
        mySensorViewModel.navigationEvent.collect { screen ->
            navController.navigate(screen)
        }
    }

    LaunchedEffect(showError) {
        if (showError) {
            errorPopUp(
                context = context,
                message = errorMessage,
                backgroundColor = backgroundColor,
                textColor = textColor,
                strokeColor = strokeColor
            )
            delay(3000)
            mySensorViewModel.setShowErrorMessage(false)
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
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Dashboard,
                            onClick = { mySensorViewModel.switchToScreen(Screen.Dashboard) },
                            icon = { Icon(painter = painterResource(id = R.drawable.lists_24px), contentDescription = "Dashboard") },
                            label = { Text("Dashboard") }
                        )
                        NavigationBarItem(
                            selected = currentScreen is Screen.Map,
                            onClick = { mySensorViewModel.switchToScreen(Screen.Map) },
                            icon = { Icon(Icons.Filled.Map, contentDescription = "Map") },
                            label = { Text("Map") }
                        )
                    }
                }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                        .zIndex(0f),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screen.Dashboard -> HomeScreen(
                            settingsItems = settingsItems,
                            isRefreshing = isRefreshing,
                            onRefresh = mySensorViewModel::refresh
                        )
                        Screen.Map -> {
                            // Первый запуск: если не даны — запрашиваем
                            LaunchedEffect(Unit) {
                                if (!allPermissionsGranted) {
                                    locationPermissionsState.launchMultiplePermissionRequest()
                                } else {
                                    mapViewModel.updateCurrentLocation(context)
                                }
                            }

                            // Если статус разрешений изменился
                            LaunchedEffect(locationPermissionsState.permissions) {
                                if (allPermissionsGranted) {
                                    mapViewModel.updateCurrentLocation(context)
                                }
                            }
                            MapScreen(
                                mapViewModel = mapViewModel,
                                currentLocation = currentLocation,
                                context = context,
                                settingsItems = settingsItems,
                                onAddToDashboard = { sensorId, address ->
                                    mapViewModel.buildSettingsSensorFromMap(sensorId, address) {settingsSensor ->
                                        mySensorViewModel.addSensorDashboardFromMap(settingsSensor)
                                    }
                                },
                                onRemoveFromDashboard = { id ->
                                    mySensorViewModel.removeSensorDashboardFromMap(id)
                                }
                            )
                        }
                    }
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
