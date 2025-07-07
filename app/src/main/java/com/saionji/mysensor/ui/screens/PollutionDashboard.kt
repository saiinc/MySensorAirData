package com.saionji.mysensor.ui.screens
/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saionji.mysensor.data.SettingsSensor
import com.saionji.mysensor.R

@Composable
fun PollutionDashboard(
    modfier: Modifier,
    pollutionDataList: State<List<SettingsSensor>>
) {
    //if (pollutionDataList.value.size == 1 &&
    //    pollutionDataList.value[0].id.isBlank() &&
    //    pollutionDataList.value[0].description.isBlank()
    if (pollutionDataList.value.isEmpty()
    ) {
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
                PollutionGrid(data = pollutionDataList.value[index])
            }
        }
    }
}

// Компонент для отображения одной карточки

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PollutionGrid(data: SettingsSensor) {
    Column(
        modifier = Modifier
            .padding(12.dp)
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                BorderStroke(2.dp, color = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        // Заголовок и id
        Row(
            modifier = Modifier
                .padding(start = 22.dp, top = 12.dp, end = 14.dp)
                .fillMaxWidth()
        ) {
            data.description?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .weight(1f)  // Занимает оставшееся место
                        .padding(end = 8.dp),
                    maxLines = Int.MAX_VALUE
                )
            }
            data.id?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.Top)  // Выравнивание по верхнему краю
                )
            }
        }

        // Сетка значений
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            //mainAxisAlignment = FlowMainAxisAlignment.Start,
        ) {
            data.deviceSensors?.forEach { sensor ->
                Card(
                    modifier = Modifier
                        .padding(start = 28.dp)
                        .size(145.dp)
                        .requiredHeight(116.dp),
                    ) {
                    Box {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            sensor.valueType?.let {
                                Text(
                                    modifier = Modifier
                                        .padding(top = 8.dp),
                                    text = it,
                                    textAlign = TextAlign.Center,
                                    fontSize = 21.sp
                                )
                            }
                            sensor.value?.let {
                                Text(
                                    modifier = Modifier
                                        .padding(bottom = 14.dp),
                                    text = it,
                                    textAlign = TextAlign.Center,
                                    fontSize = 21.sp,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp, top = 8.dp)
                                .size(15.dp)
                                .clip(CircleShape)
                                .align(TopEnd)
                                .background(color = sensor.color)
                        )
                    }
                }
            }
        }
    }
}