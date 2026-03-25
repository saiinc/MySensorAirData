package com.saionji.mysensor.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.saionji.mysensor.shared.domain.model.LatLng
import com.saionji.mysensor.shared.data.model.DashboardSensor
import com.saionji.mysensor.shared.domain.C
import com.saionji.mysensor.shared.ui.map.MapController
import com.saionji.mysensor.shared.ui.map.MapScreenContent
import com.saionji.mysensor.shared.ui.map.MapUiState

@Composable
fun MapScreen(
    mapViewModel: AndroidMapViewModel,
    currentLocation: LatLng?,
    dashboardSensors: State<List<DashboardSensor>>,
    onAddToDashboard: (String, String) -> Unit,
    onRemoveFromDashboard: (String) -> Unit
) {
    val mapController = remember { mutableStateOf<MapController?>(null) }
    val mapUiState by mapViewModel.mapUiState.collectAsState()
    val selectedValueType by mapViewModel.selectedValueType.collectAsState()
    val selectedMarker by mapViewModel.selectedMarker.collectAsState()
    val addresses by mapViewModel.addresses.collectAsState()
    val camera by mapViewModel.cameraState.collectAsState()

    // Синхронизация камеры
    LaunchedEffect(camera, mapController.value) {
        val controller = mapController.value ?: return@LaunchedEffect
        val state = camera ?: return@LaunchedEffect
        if (!state.isProgrammatic) return@LaunchedEffect
        controller.moveTo(state.lat, state.lon, state.zoom)
    }

    // Используем MapScreenContent с MapLibreView как слот
    MapScreenContent(
        selectedValueType = selectedValueType,
        selectedMarker = selectedMarker,
        addresses = addresses,
        isLimitReached = dashboardSensors.value.size > C.DASHBOARD_SENSOR_LIMIT,
        isMarkerAdded = selectedMarker?.let { marker ->
            dashboardSensors.value.any { it.id == marker.id }
        } ?: false,

        // === SLOTS: MapLibreView передаётся как слот ===
        mapView = {
            MapLibreView(
                modifier = Modifier.fillMaxSize(),
                markers = (mapUiState as? MapUiState.Success)?.markers ?: emptyList(),
                onMapReady = { controller ->
                    mapController.value = controller
                    controller.setOnViewportChangedListener { bounds ->
                        mapViewModel.onCameraMovedFromUser(bounds)
                        mapViewModel.onViewportChanged(bounds)
                    }
                    mapViewModel.cameraState.value?.let { state ->
                        controller.moveTo(state.lat, state.lon, state.zoom)
                    }
                },
                onMarkerClick = { markerId ->
                    val state = mapUiState
                    if (state is MapUiState.Success) {
                        val marker = state.markers.firstOrNull { it.id == markerId }
                            ?: return@MapLibreView
                        mapViewModel.onMarkerSelected(marker)
                    }
                },
                onMapClick = {
                    mapViewModel.clearSelectedMarker()
                }
            )
        },

        // === Callbacks ===
        onZoomIn = { mapController.value?.zoomIn() },
        onZoomOut = { mapController.value?.zoomOut() },
        onMyLocation = {
            currentLocation?.let {
                mapController.value?.moveTo(it.lat, it.lon, 12.0)
            }
        },
        onValueTypeSelected = { mapViewModel.setSelectedValueType(it) },
        onMarkerClose = { mapViewModel.clearSelectedMarker() },
        onMarkerAdd = {
            selectedMarker?.let { marker ->
                val address = addresses["${marker.lat},${marker.lon}"]
                if (address != null) {
                    onAddToDashboard(marker.id, address)
                }
            }
        },
        onMarkerRemove = {
            selectedMarker?.let { marker ->
                onRemoveFromDashboard(marker.id)
            }
        }
    )
}