/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import com.saionji.mysensor.data.DashboardSensor
import com.saionji.mysensor.data.SettingsApp


@Composable
fun ShareScreen(
    settingsApp: SettingsApp,
    settingsItems: State<List<DashboardSensor>>,
    onImageGenerated: (ImageBitmap?) -> Unit
) {
    var graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()
    var contentHeight by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier
        .size(0.dp) // size 0 so that no space is used in the UI
        .drawWithCache {
            // draw to graphics layer
            graphicsLayer = obtainGraphicsLayer().apply {
                record(
                    size = IntSize(
                        width = 400.dp.toPx().toInt(),
                        height = contentHeight
                    )
                ) {
                    drawContent()
                }
            }
            // leave blank to skip drawing on the screen
            onDrawWithContent { }
        }) {
        Box(
            // override the parent size with desired size of the recording
            modifier = Modifier
                .wrapContentHeight(unbounded = true, align = Alignment.Top)
                .wrapContentWidth(unbounded = true, align = Alignment.Start)
                .requiredWidth(400.dp)
        ) {
            // The content being recorded
            Surface(modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { layoutCoordinates ->
                    // Измеряем высоту содержимого
                    contentHeight = layoutCoordinates.size.height
                }
            ){
                PollutionDashboardShare(
                    settingsApp = settingsApp,
                    pollutionDataList = settingsItems
                )
            }
        }
    }
    // Сохраняем Bitmap при загрузке экрана
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val image = graphicsLayer.toImageBitmap()
            onImageGenerated(image)
        }
    }
}