/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */
package com.saionji.mysensor.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saionji.mysensor.R
import com.saionji.mysensor.shared.data.model.DashboardSensor

@Composable
fun PollutionDashboard(
    modifier: Modifier,
    pollutionDataList: State<List<DashboardSensor>>
) {
    if (pollutionDataList.value.isEmpty()
    ) {
        HorizontalDivider(modifier = modifier.padding(all = 2.dp))
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.add_sensor),
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Рендерим карточки для каждого элемента списка
            items(pollutionDataList.value.size) { index ->
                HorizontalDivider()
                PollutionGrid(data = pollutionDataList.value[index])
            }
        }
    }
}

// Компонент для отображения одной карточки

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PollutionGrid(data: DashboardSensor) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Заголовок и id
        Row(
            modifier = Modifier
                .padding(start = 22.dp, top = 8.dp, end = 14.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = data.description,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .weight(1f)  // Занимает оставшееся место
                    .padding(end = 8.dp),
                maxLines = Int.MAX_VALUE
            )
            Text(
                text = data.id,
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Top)  // Выравнивание по верхнему краю
            )
        }

        // Сетка значений
        ValuesGrid(data)
    }
}

@Composable
fun ValuesGrid(data: DashboardSensor) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
    ) {
        if (data.isLoading) {
            repeat(2) {
                SkeletonValueCard()
            }
        } else {
            data.deviceSensors.forEach { sensor ->
                Card(
                    modifier = Modifier
                        .padding(start = 22.dp, bottom = 6.dp)
                        .requiredWidth(155.dp)
                        .requiredHeight(102.dp),
                ) {
                    Box {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            Text(
                                modifier = Modifier
                                    .padding(top = 8.dp),
                                text = sensor.valueType,
                                textAlign = TextAlign.Center,
                                fontSize = 21.sp
                            )
                            Text(
                                modifier = Modifier
                                    .padding(bottom = 10.dp),
                                text = sensor.value,
                                textAlign = TextAlign.Center,
                                fontSize = 21.sp,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp, top = 8.dp)
                                .size(15.dp)
                                .clip(CircleShape)
                                .align(TopEnd)
                                .background(color = Color(sensor.color))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonValueCard() {
    Card(
        modifier = Modifier
            .padding(start = 22.dp, bottom = 6.dp)
            .requiredWidth(155.dp)
            .requiredHeight(102.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmerEffect()
        )
    }
}

@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )

    return this.background(brush)
}