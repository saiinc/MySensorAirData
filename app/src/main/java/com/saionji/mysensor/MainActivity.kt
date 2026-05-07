/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.saionji.mysensor.ui.SensorsApp
import com.saionji.mysensor.shared.ui.theme.SensorsAppTheme
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.module.http.HttpRequestUtil

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
        HttpRequestUtil.setOkHttpClient(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .header("User-Agent", BuildConfig.MAPTILER_USER_AGENT)
                        .build()
                    chain.proceed(request)
                }
                .build()
        )
        setContent {
            SensorsAppTheme {
                // A surface container using the 'background' color from the theme
                SensorsApp()
            }
        }
    }
}

