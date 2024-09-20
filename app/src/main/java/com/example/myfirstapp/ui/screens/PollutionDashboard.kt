package com.example.myfirstapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfirstapp.data.MyDevice
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode

@Composable
fun PollutionDashboard(pollutionDataList: List<MyDevice>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        //verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Рендерим карточки для каждого элемента списка
        items(pollutionDataList.size) {index ->
            PollutionGrid(data = pollutionDataList[index])
        }
    }
}

// Компонент для отображения одной карточки
@Composable
fun PollutionGrid(data: MyDevice) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.Unspecified, shape = RoundedCornerShape(8.dp))
            .border(
                BorderStroke(2.dp, Color.LightGray),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        //horizontalAlignment = Alignment.Start
    ) {
        // Заголовок и значение
        Row(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    top = 8.dp,
                    end = 8.dp
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
            data.id?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        // Сетка значений
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp,
                    end = 8.dp
                ),
            mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween,
        ) {
            data.deviceSensors.forEach { sensor ->
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(145.dp)
                        .requiredHeight(116.dp),
                    ) {
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
                }
            }
        }
    }
}