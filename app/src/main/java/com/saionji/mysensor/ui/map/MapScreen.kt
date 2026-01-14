package com.saionji.mysensor.ui.map

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saionji.mysensor.data.SettingsSensor
import com.saionji.mysensor.ui.map.model.withPadding
import com.saionji.mysensor.ui.map.renderer.MapLibreMarkerRenderer
import org.maplibre.android.geometry.LatLng

@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    currentLocation: LatLng?,
    context: Context,
    settingsItems: State<List<SettingsSensor>>,
    onAddToDashboard: (String, String) -> Unit,
    onRemoveFromDashboard: (String) -> Unit
) {

    val mapController = remember { mutableStateOf<MapController?>(null) }
    val markerRenderer = remember { mutableStateOf<MapLibreMarkerRenderer?>(null) }

    val mapUiState by mapViewModel.mapUiState.collectAsState()

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

                // ⬅️ создаём renderer ЗДЕСЬ
                markerRenderer.value = renderer

                // 1️⃣ центрируем карту
                currentLocation?.let {
                    controller.moveTo(
                        lat = it.latitude,
                        lon = it.longitude,
                        zoom = 10.5
                    )
                }

                // 2️⃣ подписываемся на viewport
                controller.setOnViewportChangedListener { bounds ->
                    mapViewModel.onViewportChanged(bounds.withPadding(0.03))
                }
            }
        )

        // 🔜 позже: MarkerPopup, кнопки, цветовая шкала
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(onClick = { mapController.value?.zoomIn() }) { Text("+") }
            FloatingActionButton(onClick = { mapController.value?.zoomOut() }) { Text("–") }
        }
    }
}