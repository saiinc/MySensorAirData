package com.saionji.mysensor.ui.screens

import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp

@Composable
fun VerticalColorBar(
    colorRanges: List<Triple<Double, Double, Triple<Int, Int, Int>>>,
    modifier: Modifier = Modifier
) {
    val gradientColors = colorRanges.map {
        Color(it.third.first, it.third.second, it.third.third)
    }.reversed()

    val minValue = colorRanges.first().first
    val maxValue = colorRanges.last().second

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val brush = Brush.verticalGradient(colors = gradientColors)
            drawRect(brush = brush)

            // Подписи с использованием drawContext.canvas.nativeCanvas
            val step = (maxValue - minValue) / 4
            for (i in 0..4) {
                val y = size.height * i / 4f
                drawContext.canvas.nativeCanvas.drawText(
                    String.format("%.0f", maxValue - i * step),
                    size.width - 10.dp.toPx(), // немного левее
                    y + 5.dp.toPx(), // чуть ниже
                    Paint().apply {
                        textAlign = android.graphics.Paint.Align.RIGHT
                        textSize = 32f
                    }
                )
            }
        }
    }
}