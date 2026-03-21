package com.saionji.mysensor.shared.ui.theme

import androidx.compose.runtime.Composable

@Composable
actual fun getPlatformColorScheme(
    darkTheme: Boolean
): androidx.compose.material3.ColorScheme {
    // iOS использует фиксированные фиолетовые цвета
    // TODO: В будущем можно добавить поддержку iOS системных цветов
    return if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }
}

@Composable
actual fun setupStatusBar(
    colorScheme: androidx.compose.material3.ColorScheme,
    darkTheme: Boolean
) {
    // iOS не требует явного управления статус баром
    // Система автоматически адаптирует цвет
}