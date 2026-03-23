package com.saionji.mysensor.shared.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text

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

    BoxWithConstraints(modifier = modifier) {
        val containerHeight = maxHeight

        // Рисуем градиент
        Canvas(modifier = Modifier
            .width(10.dp)
            .height(180.dp)
            .align(Alignment.CenterEnd)
        ) {
            val brush = Brush.verticalGradient(colors = gradientColors)
            drawRect(brush = brush)
        }

        // Текстовые подписи
        val step = (maxValue - minValue) / 5
        for (i in 0..5) {
            val yPosition = containerHeight * i / 5f

            Text(
                text = (maxValue - i * step).toInt().toString(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-15).dp, y = yPosition - 90.dp), // Смещаем влево и по Y
                textAlign = androidx.compose.ui.text.style.TextAlign.End,
                fontSize = 12.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            )
        }
    }
}