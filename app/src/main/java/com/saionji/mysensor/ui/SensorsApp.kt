/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui

import android.Manifest
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.painterResource
import com.saionji.mysensor.shared.generated.resources.Res
import com.saionji.mysensor.shared.generated.resources.lists_24px
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.saionji.mysensor.shared.generated.resources.error_loading
import com.saionji.mysensor.shared.generated.resources.error_unknown
import com.saionji.mysensor.shared.ui.ShareManager
import com.saionji.mysensor.shared.ui.components.ErrorBanner
import com.saionji.mysensor.shared.ui.components.MainAppBar
import com.saionji.mysensor.shared.ui.components.OptionsBoxState
import com.saionji.mysensor.ui.map.AndroidMapViewModel
import com.saionji.mysensor.shared.ui.screens.AboutScreen
import com.saionji.mysensor.shared.ui.screens.HomeScreen
import com.saionji.mysensor.shared.ui.viewmodel.MySensorViewModel
import com.saionji.mysensor.shared.ui.viewmodel.Screen
import com.saionji.mysensor.ui.map.MapScreen
import com.saionji.mysensor.shared.ui.screens.ShareScreen
import org.jetbrains.compose.resources.stringResource
import com.saionji.mysensor.shared.ui.navigation.NavigationDestination

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SensorsApp(
    modifier: Modifier = Modifier
) {
    val mySensorViewModel: AndroidMySensorViewModel =
        viewModel(factory = AndroidMySensorViewModel.Factory)
    val mapViewModel: AndroidMapViewModel =
        viewModel(factory = AndroidMapViewModel.Factory)
    val optionsBoxState = mySensorViewModel.optionsBoxState
    val settingsApp = mySensorViewModel.settingsApp.value
    val settingsItems = mySensorViewModel.dashboardItems.collectAsState()
    val sensorsOptions = mySensorViewModel.sensorsOptions.collectAsState()
    val isRefreshing = mySensorViewModel.isRefreshing.collectAsState().value
    val error by mySensorViewModel.error.collectAsState()
    val currentScreen by mySensorViewModel.currentScreen.collectAsState()
    val currentLocation by mapViewModel.currentLocation

    val context = LocalContext.current

    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    val allPermissionsGranted = locationPermissionsState.permissions.all { it.status.isGranted }
    val shareManager = remember { ShareManager(context) }

    val navController = rememberNavController()

    // Подписка на навигационные события
    LaunchedEffect(Unit) {
        mySensorViewModel.navigationEvent.collect { destination ->
            navController.navigate(destination.toRoute())
        }
    }

    val errorMessage = when (error) {
        MySensorViewModel.ErrorType.Network -> stringResource(Res.string.error_loading)
        MySensorViewModel.ErrorType.Unknown -> stringResource(Res.string.error_unknown)
        null -> null
    }

    if (errorMessage != null) {
        ErrorBanner(message = errorMessage)
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
                            mySensorViewModel.updateSensorOptionsItemId(
                                index = index,
                                settingsSensorItemId = settingsSensorItemId
                            )
                        },
                        onEditSensorDescription = { index, settingsSensorItemDescription ->
                            mySensorViewModel.updateSensorOptionsItemDescription(
                                index = index,
                                settingsSensorItemDescription = settingsSensorItemDescription
                            )
                        },
                        onOptionsBoxTriggered = {
                            mySensorViewModel.sensorsOptionsLoad()
                            mySensorViewModel.updateOptionsBoxState(newValue = OptionsBoxState.OPENED)
                        },
                        onAboutClicked = {
                            mySensorViewModel.navigateTo(NavigationDestination.About)
                        },
                        onShareClicked = { mySensorViewModel.setShowShareScreen(true) },
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentScreen is Screen.Dashboard,
                            onClick = { mySensorViewModel.switchToScreen(Screen.Dashboard) },
                            icon = { Icon(painter = painterResource(Res.drawable.lists_24px), contentDescription = "Dashboard") },
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
                            dashboardSensor = settingsItems,
                            isRefreshing = isRefreshing,
                            onRefresh = mySensorViewModel::refresh
                        )
                        Screen.Map -> {
                            // Первый запуск: если не даны — запрашиваем
                            LaunchedEffect(Unit) {
                                if (!allPermissionsGranted) {
                                    locationPermissionsState.launchMultiplePermissionRequest()
                                } else {
                                    mapViewModel.updateCurrentLocation()
                                }
                            }

                            // Если статус разрешений изменился
                            LaunchedEffect(locationPermissionsState.permissions) {
                                if (allPermissionsGranted) {
                                    mapViewModel.updateCurrentLocation()
                                }
                            }
                            MapScreen(
                                mapViewModel = mapViewModel,
                                dashboardSensors = settingsItems,
                                onAddToDashboard = { sensorId, address ->
                                    mapViewModel.buildSettingsSensorFromMap(
                                        sensorId,
                                        address
                                    ) { settingsSensor ->
                                        mySensorViewModel.addSensorDashboardFromMap(settingsSensor)
                                    }
                                },
                                onRemoveFromDashboard = { id ->
                                    mySensorViewModel.removeSensorDashboardFromMap(id)
                                },
                                currentLocation = currentLocation
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
            onImageGenerated = { image ->
                if (image != null) {
                    shareManager.share(image)
                    }
                mySensorViewModel.setShowShareScreen(false) // Закрываем ShareScreen
            }
        )
    }
}
