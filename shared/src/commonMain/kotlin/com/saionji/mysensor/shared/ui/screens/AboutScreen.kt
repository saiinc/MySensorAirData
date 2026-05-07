package com.saionji.mysensor.shared.ui.screens
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
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.saionji.mysensor.shared.generated.resources.Res
import com.saionji.mysensor.shared.generated.resources.about_app
import com.saionji.mysensor.shared.generated.resources.about_app_description
import com.saionji.mysensor.shared.generated.resources.about_app_link_intro
import com.saionji.mysensor.shared.generated.resources.about_back
import com.saionji.mysensor.shared.generated.resources.about_color_ranges_note
import com.saionji.mysensor.shared.generated.resources.about_health_implications
import com.saionji.mysensor.shared.generated.resources.about_quality_good
import com.saionji.mysensor.shared.generated.resources.about_quality_good_desc
import com.saionji.mysensor.shared.generated.resources.about_quality_hazardous
import com.saionji.mysensor.shared.generated.resources.about_quality_hazardous_desc
import com.saionji.mysensor.shared.generated.resources.about_quality_moderate
import com.saionji.mysensor.shared.generated.resources.about_quality_moderate_desc
import com.saionji.mysensor.shared.generated.resources.about_quality_unhealthy
import com.saionji.mysensor.shared.generated.resources.about_quality_unhealthy_desc
import com.saionji.mysensor.shared.generated.resources.about_quality_unhealthy_sensitive
import com.saionji.mysensor.shared.generated.resources.about_quality_unhealthy_sensitive_desc
import com.saionji.mysensor.shared.generated.resources.about_quality_very_unhealthy
import com.saionji.mysensor.shared.generated.resources.about_quality_very_unhealthy_desc
import com.saionji.mysensor.shared.generated.resources.about_range_higher
import com.saionji.mysensor.shared.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

private const val ABOUT_REPO_URL = "https://github.com/saiinc/MySensorAirData"
private const val SENSOR_COMMUNITY_URL = "https://sensor.community/en/sensors/airrohr/"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class)
@Composable
fun AboutScreen(onBackClicked: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val annotatedText = buildAnnotatedString {
        withStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.onBackground
            )
        ) {
            append(stringResource(Res.string.about_app_link_intro))

            withStyle(SpanStyle(color = Color.Blue)) {
                withAnnotation("url", SENSOR_COMMUNITY_URL) {
                    append(SENSOR_COMMUNITY_URL)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.about_app)) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.about_back)
                        )
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
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = stringResource(Res.string.about_app_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(
                    text = stringResource(Res.string.about_health_implications),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    AirQualityCard(
                        color = Color(0xFF00E400),
                        label = stringResource(Res.string.about_quality_good),
                        pm25Range = "0-12 µg/m³",
                        pm10Range = "0-54 µg/m³",
                        description = stringResource(Res.string.about_quality_good_desc)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFFFFE600),
                        label = stringResource(Res.string.about_quality_moderate),
                        pm25Range = "13-35 µg/m³",
                        pm10Range = "55-154 µg/m³",
                        description = stringResource(Res.string.about_quality_moderate_desc)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFFFF7E00),
                        label = stringResource(Res.string.about_quality_unhealthy_sensitive),
                        pm25Range = "36-56 µg/m³",
                        pm10Range = "155-254 µg/m³",
                        description = stringResource(Res.string.about_quality_unhealthy_sensitive_desc)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFFFE0000),
                        label = stringResource(Res.string.about_quality_unhealthy),
                        pm25Range = "57-151 µg/m³",
                        pm10Range = "255-354 µg/m³",
                        description = stringResource(Res.string.about_quality_unhealthy_desc)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFF98004B),
                        label = stringResource(Res.string.about_quality_very_unhealthy),
                        pm25Range = "152-251 µg/m³",
                        pm10Range = "355-424 µg/m³",
                        description = stringResource(Res.string.about_quality_very_unhealthy_desc)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AirQualityCard(
                        color = Color(0xFF7E0023),
                        label = stringResource(Res.string.about_quality_hazardous),
                        pm25Range = "252-${stringResource(Res.string.about_range_higher)} µg/m³",
                        pm10Range = "425-${stringResource(Res.string.about_range_higher)} µg/m³",
                        description = stringResource(Res.string.about_quality_hazardous_desc)
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(
                    text = stringResource(Res.string.about_color_ranges_note),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                ClickableText(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                    onClick = { offset ->
                        annotatedText.getStringAnnotations("url", offset, offset)
                            .firstOrNull()?.let { annotation ->
                                uriHandler.openUri(annotation.item)
                            }
                    }
                )
                Text(
                    text = ABOUT_REPO_URL,
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.secondary),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
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
                }
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp)
                ) {
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

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
        }
    }
}
