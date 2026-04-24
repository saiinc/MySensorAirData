@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.saionji.mysensor.shared

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.ComposeUIViewController
import com.saionji.mysensor.shared.di.IosContainer
import com.saionji.mysensor.shared.ui.app.SensorsAppContent
import com.saionji.mysensor.shared.ui.map.IosMapScreen
import com.saionji.mysensor.shared.ui.ShareManager
import com.saionji.mysensor.shared.ui.viewmodel.MySensorViewModel
import androidx.compose.runtime.collectAsState
import com.saionji.mysensor.shared.platform.PermissionService
import platform.CoreImage.CIContext
import platform.UIKit.UIViewController

private lateinit var iosContainer: IosContainer

@Suppress("unused")
fun MainViewController(): UIViewController {
    iosContainer = IosContainer()
    val permissionService = PermissionService()
    val context = CIContext
    val shareManager = ShareManager()

    return ComposeUIViewController {
        val currentLocation by iosContainer.mapViewModel.currentLocation
        val viewModel = MySensorViewModel(
            settingsRepository = iosContainer.settingsRepository,
            getSensorValuesUseCase = iosContainer.getSensorValuesUseCase
        )

        SensorsAppContent(
            mySensorViewModel = viewModel,
            mapScreen = {
                IosMapScreen(
                    mapViewModel = iosContainer.mapViewModel,
                    currentLocation = currentLocation,
                    dashboardSensors = viewModel.dashboardItems.collectAsState(),
                    onAddToDashboard = { id, desc ->
                        iosContainer.mapViewModel.buildSettingsSensorFromMap(
                            id, desc,
                            { settingsSensor ->
                                viewModel.addSensorDashboardFromMap(settingsSensor)})
                    },
                    onRemoveFromDashboard = { id ->
                        viewModel.removeSensorDashboardFromMap(id)
                    }
                )
            },
            permissionHandler = {
                LaunchedEffect(Unit) {
                    if (permissionService.requestLocationPermissions(
                            context = context
                        )) {
                        iosContainer.mapViewModel.updateCurrentLocation()
                    }
                }
            },
            onShare = { image ->
                image?.let { shareManager.share(it) }
            }
        )
    }
}
