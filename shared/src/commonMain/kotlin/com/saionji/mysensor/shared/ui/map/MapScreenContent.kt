package com.saionji.mysensor.shared.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saionji.mysensor.shared.domain.HUMIDITY_COLOR_RANGES
import com.saionji.mysensor.shared.domain.NOISE_COLOR_RANGES
import com.saionji.mysensor.shared.domain.PM10_COLOR_RANGES
import com.saionji.mysensor.shared.domain.PM25_COLOR_RANGES
import com.saionji.mysensor.shared.domain.PRESSURE_COLOR_RANGES
import com.saionji.mysensor.shared.domain.TEMPERATURE_COLOR_RANGES
import com.saionji.mysensor.shared.domain.model.MapMarker

/**
 * Cross-platform MapScreen content with Slot API
 *
 * @param mapView Slot for platform-specific map implementation
 */
@Composable
fun MapScreenContent(
    selectedValueType: String,
    selectedMarker: MapMarker?,
    addresses: Map<String, String>,
    isLimitReached: Boolean,
    isMarkerAdded: Boolean,
    mapView: @Composable () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onMyLocation: () -> Unit,
    onValueTypeSelected: (String) -> Unit,
    onMarkerClose: () -> Unit,
    onMarkerAdd: () -> Unit,
    onMarkerRemove: () -> Unit
) {
    val valueTypes = listOf(
        "PM2.5", "PM10", "temperature", "humidity", "pressure", "noise LAeq"
    )

    var expanded by remember { mutableStateOf(false) }

    fun rangesForType(type: String) = when (type) {
        "PM2.5" -> PM25_COLOR_RANGES
        "PM10" -> PM10_COLOR_RANGES
        "temperature" -> TEMPERATURE_COLOR_RANGES
        "humidity" -> HUMIDITY_COLOR_RANGES
        "pressure" -> PRESSURE_COLOR_RANGES
        "noise LAeq" -> NOISE_COLOR_RANGES
        else -> PM25_COLOR_RANGES
    }

    val gradientRanges = rangesForType(selectedValueType)

    Box(modifier = Modifier.fillMaxSize()) {

        // === SLOTS: Карта вставляется здесь ===
        mapView()

        // === Кнопки зума ===
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                modifier = Modifier.size(50.dp),
                onClick = onZoomIn
            ) { Text("+") }

            FloatingActionButton(
                modifier = Modifier.size(50.dp),
                onClick = onZoomOut
            ) { Text("–") }
        }

        // === Цветовая шкала ===
        VerticalColorBar(
            colorRanges = gradientRanges,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)
                .width(30.dp)
                .height(180.dp),
        )

        // === FAB "Моё местоположение" ===
        FloatingActionButton(
            modifier = Modifier
                .size(50.dp)
                .padding(start = 8.dp, top = 8.dp),
            onClick = onMyLocation
        ) {
            Icon(Icons.Default.MyLocation, null)
        }

        // === Dropdown выбора типа сенсора ===
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Button(
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "⋮ $selectedValueType",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                valueTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            onValueTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        // === Popup выбранного маркера ===
        selectedMarker?.let { marker ->
            val address = addresses["${marker.lat},${marker.lon}"]

            MarkerPopup(
                marker = marker,
                address = address,
                isAdded = isMarkerAdded,
                isLimitReached = isLimitReached,
                onClose = onMarkerClose,
                onAdd = onMarkerAdd,
                onRemove = onMarkerRemove
            )
        }
    }
}