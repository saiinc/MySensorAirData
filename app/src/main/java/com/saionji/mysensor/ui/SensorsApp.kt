/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.saionji.mysensor.shared.ui.ShareManager
import com.saionji.mysensor.shared.ui.app.SensorsAppContent
import com.saionji.mysensor.ui.map.AndroidMapViewModel
import com.saionji.mysensor.ui.map.MapScreen

/**
 * Android wrapper для SensorsApp
 *
 * Отвечает ТОЛЬКО за:
 * - Создание ViewModel (Android Lifecycle)
 * - Управление Permissions (Accompanist)
 * - Создание ShareManager (Android Context)
 * - Создание MapScreen (MapLibre Android)
 *
 * Вся остальная логика в SensorsAppContent (shared)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SensorsApp(
    modifier: Modifier = Modifier
) {
    // === ANDROID-SPECIFIC: ViewModel creation ===
    val mySensorViewModel: AndroidMySensorViewModel =
        viewModel(factory = AndroidMySensorViewModel.Factory)
    val mapViewModel: AndroidMapViewModel =
        viewModel(factory = AndroidMapViewModel.Factory)

    // === ANDROID-SPECIFIC: Context ===
    val context = LocalContext.current

    // === ANDROID-SPECIFIC: Permissions ===
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    )

    // === ANDROID-SPECIFIC: ShareManager ===
    val shareManager = remember { ShareManager(context) }

    // === ANDROID-SPECIFIC: Map state ===
    val currentLocation by mapViewModel.currentLocation
    val settingsItems = mySensorViewModel.sharedViewModel.dashboardItems.collectAsState()

    // === Используем SensorsAppContent с ViewModel напрямую ===
    SensorsAppContent(
        mySensorViewModel = mySensorViewModel.sharedViewModel,  // ✅ Передаём ViewModel
        mapScreen = {
            // === ANDROID-SPECIFIC: MapScreen (MapLibre) ===
            MapScreen(
                mapViewModel = mapViewModel,
                dashboardSensors = settingsItems,
                onAddToDashboard = { sensorId, address ->
                    mapViewModel.buildSettingsSensorFromMap(sensorId, address) { settingsSensor ->
                        mySensorViewModel.sharedViewModel.addSensorDashboardFromMap(settingsSensor)
                    }
                },
                onRemoveFromDashboard = { id ->
                    mySensorViewModel.sharedViewModel.removeSensorDashboardFromMap(id)
                },
                currentLocation = currentLocation
            )
        },
        permissionHandler = {
            // === ANDROID-SPECIFIC: Permissions logic ===
            LaunchedEffect(Unit) {
                if (!locationPermissionsState.allPermissionsGranted) {
                    locationPermissionsState.launchMultiplePermissionRequest()
                }
            }
            LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
                if (locationPermissionsState.allPermissionsGranted) {
                    mapViewModel.updateCurrentLocation()
                }
            }
        },
        onShare = { image ->
            // === ANDROID-SPECIFIC: Share with ShareManager ===
            if (image != null) {
                shareManager.share(image)
            }
        },
        modifier = modifier
    )
}
