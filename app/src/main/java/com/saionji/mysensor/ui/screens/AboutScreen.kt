package com.saionji.mysensor.ui.screens
/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBackClicked: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About app") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Заголовок приложения
                Text(
                    text = "My Sensor",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )

                // Описание приложения
                Text(
                    text = "This app retrieves PM and Temperature/Humidity/Pressure data from sensor.community",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Text(
                    text = "https://github.com/saiinc/MySensorAirData",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Заголовок раздела
                Text(
                    text = "Health Implications",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Уровни качества воздуха
                Column(modifier = Modifier.fillMaxWidth()) {
                    AirQualityCard(
                        color = Color(0xFF00E400),
                        label = "Good",
                        pm25Range = "0-12 µg/m³",
                        pm10Range = "0-54 µg/m³",
                        description = "Air quality is considered satisfactory, and air pollution poses little or no risk."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFFFFE600),
                        label = "Moderate",
                        pm25Range = "13-35 µg/m³",
                        pm10Range = "55-154 µg/m³",
                        description = "Air quality is acceptable; however, there may be a moderate health concern for some people."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFFFF7E00),
                        label = "Unhealthy for Sensitive Groups",
                        pm25Range = "36-56 µg/m³",
                        pm10Range = "155-254 µg/m³",
                        description = "Members of sensitive groups may experience health effects. The general public is not likely to be affected."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFFFE0000),
                        label = "Unhealthy",
                        pm25Range = "57-151 µg/m³",
                        pm10Range = "255-354 µg/m³",
                        description = "Everyone may begin to experience health effects; sensitive groups may experience more serious health effects."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFF98004B),
                        label = "Very Unhealthy",
                        pm25Range = "152-251 µg/m³",
                        pm10Range = "355-424 µg/m³",
                        description = "Health warnings of emergency conditions. The entire population is more likely to be affected."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFF7E0023),
                        label = "Hazardous",
                        pm25Range = "252-Higher µg/m³",
                        pm10Range = "425-Higher µg/m³",
                        description = "Health alert: everyone may experience more serious health effects."
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                // Дополнительное описание
                Text(
                    text = "Color ranges for other indicators (temperature, humidity, pressure, etc.) are taken from the sensor.community map.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = buildAnnotatedString{
                        append("Build your DIY sensor and become part of the worldwide, opendata & civictech network. With airRohr you can measure air pollution yourself: ")
                        withLink(
                            LinkAnnotation.Url(
                                "https://sensor.community/en/sensors/airrohr/",
                                TextLinkStyles(style = SpanStyle(color = Color.Blue)))
                        ) {
                            append("https://sensor.community/en/sensors/airrohr/")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun AirQualityCard(color: Color, label: String, description: String, pm25Range: String, pm10Range: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            //HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Диапазоны PM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ){
                Column {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        text = "PM2.5",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Text(
                        text = pm25Range,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    //Spacer(modifier = Modifier.height(4.dp))
                }
                Column (
                    modifier = Modifier
                        .padding(start = 20.dp)
                ){
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally),
                        text = "PM10",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                    Text(
                        text = pm10Range,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Описание
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        }
    }
}