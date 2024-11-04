package com.saionji.mysensor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "My Sensor",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF00796B) // Dark teal color for the header
                )

                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "This app retrieves PM and Temperature/Humidity/Pressure data from sensor.community\n\nhttps://github.com/saiinc/MySensorAirData",
                    style = MaterialTheme.typography.bodyMedium
                )


                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.CenterHorizontally),
                    text = "Health Implications",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Air quality descriptions
                Column(modifier = Modifier
                    .fillMaxWidth()
                ) {
                    AirQualityDescription(
                        color = Color(0xFF00E400),
                        label = "Good\nPM2.5 0-12 µg/m³, PM10 0-54 µg/m³",
                        description = "Air quality is considered satisfactory, and air pollution poses little or no risk"
                    )
                    AirQualityDescription(
                        color = Color(0xFFFFE600),
                        label = "Moderate\nPM2.5 13-35 µg/m³, PM10 55-154 µg/m³",
                        description = "Air quality is acceptable; however, for some pollutants there may be a moderate health concern for a very small number of people who are unusually sensitive to air pollution."
                    )
                    AirQualityDescription(
                        color = Color(0xFFFF7E00),
                        label = "Unhealthy for Sensitive Groups\nPM2.5 36-56 µg/m³, PM10 155-254 µg/m³",
                        description = "Members of sensitive groups may experience health effects. The general public is not likely to be affected."
                    )
                    AirQualityDescription(
                        color = Color(0xFFFE0000),
                        label = "Unhealthy\nPM2.5 57-151 µg/m³, PM10 255-354 µg/m³",
                        description = "Everyone may begin to experience health effects; members of sensitive groups may experience more serious health effects"
                    )
                    AirQualityDescription(
                        color = Color(0xFF98004B),
                        label = "Very Unhealthy\nPM2.5 152-251 µg/m³, PM10 355-424 µg/m³",
                        description = "Health warnings of emergency conditions. The entire population is more likely to be affected."
                    )
                    AirQualityDescription(
                        color = Color(0xFF7E0023),
                        label = "Hazardous\nPM2.5 252-Higher µg/m³, PM10 425-Higher µg/m³",
                        description = "Health alert: everyone may experience more serious health effects"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Additional app description
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Color ranges for other indicators (temperature, humidity, pressure, etc.) are taken from the sensor.community map.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun AirQualityDescription(color: Color, label: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )
    }
}