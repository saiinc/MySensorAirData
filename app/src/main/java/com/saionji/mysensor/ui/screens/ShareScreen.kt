/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.saionji.mysensor.ui.MySensorUiState
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.saionji.mysensor.data.SettingsApp
import java.io.File
import java.io.FileOutputStream


@Composable
fun ShareScreen(
    settingsApp: SettingsApp,
    mySensorUiState: MySensorUiState,
    onBitmapGenerated: (Bitmap?) -> Unit
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
            val pollutionDataList = if (mySensorUiState is MySensorUiState.Success) {
                mySensorUiState.getVal
            } else {
                emptyList()
            }
            Surface(modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { layoutCoordinates ->
                    // Измеряем высоту содержимого
                    contentHeight = layoutCoordinates.size.height
                }
            ){
                PollutionDashboardShare(
                    settingsApp = settingsApp,
                    pollutionDataList = pollutionDataList
                )
            }
        }
    }
    // Сохраняем Bitmap при загрузке экрана
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val myImageBitmap = graphicsLayer.toImageBitmap()
            val bitmap = myImageBitmap.asAndroidBitmap()
            onBitmapGenerated(bitmap)
        }
    }
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
    val file = File(context.cacheDir, "captured_image.png")
    FileOutputStream(file).use { output ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

fun shareUri(context: Context, uri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Поделиться изображением"))
}