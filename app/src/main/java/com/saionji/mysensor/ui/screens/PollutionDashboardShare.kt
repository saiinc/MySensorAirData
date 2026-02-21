/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saionji.mysensor.data.DashboardSensor
import com.saionji.mysensor.data.SettingsApp

@Composable
fun PollutionDashboardShare(
    settingsApp: SettingsApp,
    pollutionDataList: State<List<DashboardSensor>>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        // Рендерим карточки для каждого элемента списка
        pollutionDataList.value.forEach { pollutionDataList ->
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
    data: DashboardSensor
) {
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
                .padding(
                    start = 22.dp,
                    top = 8.dp,
                    end = 14.dp
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = data.description,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            if(!settingsApp.shareId) {
                Text(
                    text = data.id,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
        // Сетка значений
        ValuesGrid(data)
    }
}