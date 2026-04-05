@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.saionji.mysensor.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.saionji.mysensor.shared.ui.app.SensorsAppContent
import com.saionji.mysensor.shared.ui.viewmodel.MySensorViewModel
// platform.UIKit распознаётся только на macOS с Xcode
// На Windows IDE покажет "Unresolved reference" - это нормально
import platform.UIKit.UIViewController

/**
 * Entry point для iOS приложения
 *
 * Вызывается из ContentView.swift:
 * MainViewControllerKt.MainViewController()
 */
@Suppress("unused")
fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        // TODO: Подключить DI контейнер
        // Пока используем заглушку

        // Простой текст для проверки
        androidx.compose.material3.Text("MySensor iOS - Compose работает!")
    }
}