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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saionji.mysensor.data.SettingsApp
import com.saionji.mysensor.data.SettingsSensor

@Composable
fun PollutionDashboardShare(
    settingsApp: SettingsApp,
    pollutionDataList: State<List<SettingsSensor>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        // Рендерим карточки для каждого элемента списка
        pollutionDataList.value?.forEach { pollutionDataList ->
            PollutionGridShare(
                settingsApp = settingsApp,
                data = pollutionDataList
            )
        }
        Text(
            text = "https://github.com/saiinc/MySensorAirData",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp)
        )
    }
}

// Компонент для отображения одной карточки

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PollutionGridShare(
    settingsApp: SettingsApp,
    data: SettingsSensor
) {
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
                .padding(
                    start = 22.dp,
                    top = 12.dp,
                    end = 14.dp
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.description?.let {
                Text(
                    text = it,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            if(!settingsApp.shareId) {
                data.id?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }

        // Сетка значений
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        ) {
            data.deviceSensors.forEach { sensor ->
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