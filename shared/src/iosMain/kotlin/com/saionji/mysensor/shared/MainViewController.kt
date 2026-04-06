@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.saionji.mysensor.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.saionji.mysensor.shared.di.IosContainer
import com.saionji.mysensor.shared.ui.app.SensorsAppContent
import com.saionji.mysensor.shared.ui.viewmodel.MySensorViewModel
import platform.UIKit.UIViewController

private lateinit var iosContainer: IosContainer

@Suppress("unused")
fun MainViewController(): UIViewController {
    iosContainer = IosContainer()

    return ComposeUIViewController {
        val viewModel = MySensorViewModel(
            settingsRepository = iosContainer.settingsRepository,
            getSensorValuesUseCase = iosContainer.getSensorValuesUseCase
        )

        SensorsAppContent(
            mySensorViewModel = viewModel,
            mapScreen = {
                // Заглушка - карта позже
                androidx.compose.material3.Text("Map coming soon...")
            },
            permissionHandler = {
                // Заглушка - permissions позже
            },
            onShare = {
                // Заглушка - sharing позже
            }
        )
    }
}