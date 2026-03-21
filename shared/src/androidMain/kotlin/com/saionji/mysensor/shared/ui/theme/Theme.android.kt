package com.saionji.mysensor.shared.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
actual fun setupStatusBar(
    colorScheme: androidx.compose.material3.ColorScheme,
    darkTheme: Boolean
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
}