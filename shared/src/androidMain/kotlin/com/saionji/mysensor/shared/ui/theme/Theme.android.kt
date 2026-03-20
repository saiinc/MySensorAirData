package com.saionji.mysensor.shared.ui.theme

import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun getPlatformColorScheme(
    darkTheme: Boolean
): androidx.compose.material3.ColorScheme {
    val context = LocalContext.current

    return when {
        // ✅ Android 12+: Dynamic Color (системные цвета на основе обоев)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        // ✅ Android <12: Фиксированные фиолетовые цвета
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
}