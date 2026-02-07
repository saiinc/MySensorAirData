package com.saionji.mysensor.ui.map

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.saionji.mysensor.C
import com.saionji.mysensor.data.SettingsSensor
import com.saionji.mysensor.domain.HUMIDITY_COLOR_RANGES
import com.saionji.mysensor.domain.NOISE_COLOR_RANGES
import com.saionji.mysensor.domain.PM10_COLOR_RANGES
import com.saionji.mysensor.domain.PM25_COLOR_RANGES
import com.saionji.mysensor.domain.PRESSURE_COLOR_RANGES
import com.saionji.mysensor.domain.TEMPERATURE_COLOR_RANGES
import com.saionji.mysensor.domain.model.LatLng
import com.saionji.mysensor.ui.map.model.SelectedMarkerUi
import com.saionji.mysensor.ui.map.renderer.MapLibreMarkerRenderer
import com.saionji.mysensor.ui.screens.VerticalColorBar

@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    currentLocation: LatLng?,
    settingsItems: State<List<SettingsSensor>>,
    onAddToDashboard: (String, String) -> Unit,
    onRemoveFromDashboard: (String) -> Unit
) {

    val mapController = remember { mutableStateOf<MapController?>(null) }
    val markerRenderer = remember { mutableStateOf<MapLibreMarkerRenderer?>(null) }

    val mapUiState by mapViewModel.mapUiState.collectAsState()

    val valueTypes = listOf(
        "PM2.5",
        "PM10",
        "temperature",
        "humidity",
        "pressure",
        "noise LAeq"
    )

    val selectedValueType by mapViewModel.selectedValueType.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    val selectedMarker by mapViewModel.selectedMarker.collectAsState()
    val addresses by mapViewModel.addresses.collectAsState()

    val camera by mapViewModel.cameraState.collectAsState()

    LaunchedEffect(camera, mapController.value) {
        val controller = mapController.value ?: return@LaunchedEffect
        val state = camera ?: return@LaunchedEffect

        if (!state.isProgrammatic) return@LaunchedEffect

        controller.moveTo(
            state.lat,
            state.lon,
            state.zoom
        )
    }

    LaunchedEffect(mapUiState) {
        val renderer = markerRenderer.value ?: return@LaunchedEffect

        if (mapUiState is MapViewModel.MapUiState.Success) {
            renderer.showMarkers(
                (mapUiState as MapViewModel.MapUiState.Success).markers
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        MapLibreView(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { controller, renderer ->

                mapController.value = controller

                // создаём renderer
                markerRenderer.value = renderer

                // подписываемся на viewport
                controller.setOnViewportChangedListener { bounds ->
                    mapViewModel.onCameraMovedFromUser(bounds)
                    mapViewModel.onViewportChanged(bounds)
                }
                // 👇 ВАЖНО: если VM уже знает камеру — применяем
                mapViewModel.cameraState.value?.let { state ->
                    controller.moveTo(
                        state.lat,
                        state.lon,
                        state.zoom
                    )
                }
            },
            onMarkerClick = { markerId ->
                val state = mapUiState
                if (state is MapViewModel.MapUiState.Success) {
                    val marker = state.markers.firstOrNull { it.id == markerId }
                        ?: return@MapLibreView

                    mapViewModel.onMarkerSelected(
                        SelectedMarkerUi(
                            id = marker.id,
                            lat = marker.lat,
                            lon = marker.lon,
                            value = marker.value.toString(),
                            valueType = marker.valueType
                        )
                    )
                }
            },
            onMapClick = {
                mapViewModel.clearSelectedMarker()
            }
        )

        // Кнопки зума
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                modifier = Modifier.size(50.dp),
                onClick = { mapController.value?.zoomIn() }) { Text("+")
            }
            FloatingActionButton(
                modifier = Modifier.size(50.dp),
                onClick = { mapController.value?.zoomOut() }) { Text("–")
            }
        }

        fun rangesForType(type: String) =
            when (type) {
                "PM2.5" -> PM25_COLOR_RANGES
                "PM10" -> PM10_COLOR_RANGES
                "temperature" -> TEMPERATURE_COLOR_RANGES
                "humidity" -> HUMIDITY_COLOR_RANGES
                "pressure" -> PRESSURE_COLOR_RANGES
                "noise LAeq" -> NOISE_COLOR_RANGES
                else -> PM25_COLOR_RANGES
            }
        val gradientRanges = rangesForType(selectedValueType)

        VerticalColorBar(
            colorRanges = gradientRanges,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 8.dp)
                .width(10.dp)
                .height(180.dp)
                .background(
                    Color.White.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(6.dp)
                )
        )

        // Кнопка текущего местоположения
        FloatingActionButton(
            modifier = Modifier
                .size(50.dp)
                .padding(start = 8.dp, top = 8.dp),
            onClick = {
                currentLocation?.let {
                    mapController.value?.moveTo(
                        lat = it.lat,
                        lon = it.lon,
                        zoom = 12.0
                    )
                }
            }
        ) {
            Icon(Icons.Default.MyLocation, null)
        }

        // Выбор сенсора
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
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                valueTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            mapViewModel.setSelectedValueType(type)
                            expanded = false
                        }
                    )
                }
            }
        }

        selectedMarker?.let { marker ->
            val address = addresses["${marker.lat},${marker.lon}"]
            val isAdded = selectedMarker?.let { marker ->
                settingsItems.value.any { it.id == marker.id }
            } ?: false

            MarkerPopup(
                marker = marker,
                address = address,
                isAdded = isAdded,
                isLimitReached = settingsItems.value.size > C.DASHBOARD_SENSOR_LIMIT,
                onClose = { mapViewModel.clearSelectedMarker() },
                onAdd = {
                    if (address != null) {
                        onAddToDashboard(marker.id, address)
                    }
                },
                onRemove = {
                    onRemoveFromDashboard(marker.id)
                }
            )
        }
    }
}