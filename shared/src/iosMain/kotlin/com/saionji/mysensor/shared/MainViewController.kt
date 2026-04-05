@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.saionji.mysensor.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
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

        // Простой текст для проверки - по центру экрана
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("MySensor iOS - Compose работает!")
        }
    }
}