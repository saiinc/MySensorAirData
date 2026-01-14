/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.saionji.mysensor.ui.SensorsApp
import com.saionji.mysensor.ui.theme.SensorsAppTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (!resources.getBoolean(R.bool.isTablet)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(
            this,
            "", // API key НЕ нужен для open-source тайлов
            WellKnownTileServer.MapLibre
        )
        setContent {
            SensorsAppTheme {
                // A surface container using the 'background' color from the theme
                SensorsApp()
            }
        }
    }
}

