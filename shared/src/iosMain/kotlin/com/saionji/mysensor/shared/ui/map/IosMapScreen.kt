package com.saionji.mysensor.shared.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.saionji.mysensor.shared.domain.model.LatLng
import com.saionji.mysensor.shared.data.model.DashboardSensor
import com.saionji.mysensor.shared.domain.C

@Composable
fun IosMapScreen(
    mapViewModel: SharedMapViewModel,
    currentLocation: LatLng?,
    dashboardSensors: State<List<DashboardSensor>>,
    onAddToDashboard: (String, String) -> Unit,
    onRemoveFromDashboard: (String) -> Unit
) {
    val mapController = remember { mutableStateOf<MapController?>(null) }
    var pendingCenterOnMyLocation by remember { mutableStateOf(false) }
    val mapUiState by mapViewModel.mapUiState.collectAsState()
    val selectedValueType by mapViewModel.selectedValueType.collectAsState()
    val selectedMarker by mapViewModel.selectedMarker.collectAsState()
    val addresses by mapViewModel.addresses.collectAsState()
    val camera by mapViewModel.cameraState.collectAsState()
    val mapUIState by mapViewModel.mapUiState.collectAsState()

    // Синхронизация камеры
    LaunchedEffect(camera, mapController.value) {
        val controller = mapController.value ?: return@LaunchedEffect
        val state = camera ?: return@LaunchedEffect
        if (!state.isProgrammatic) return@LaunchedEffect
        controller.moveTo(state.lat, state.lon, state.zoom)
    }

    LaunchedEffect(currentLocation, pendingCenterOnMyLocation, mapController.value) {
        if (!pendingCenterOnMyLocation) return@LaunchedEffect
        val location = currentLocation ?: return@LaunchedEffect
        val controller = mapController.value ?: return@LaunchedEffect

        controller.moveTo(location.lat, location.lon, 12.0)
        pendingCenterOnMyLocation = false
    }

    MapScreenContent(
        selectedValueType = selectedValueType,
        selectedMarker = selectedMarker,
        addresses = addresses,
        isLimitReached = dashboardSensors.value.size > C.DASHBOARD_SENSOR_LIMIT,
        isMarkerAdded = selectedMarker?.let { marker ->
            dashboardSensors.value.any { it.id == marker.id }
        } ?: false,

        // === SLOTS: IosMapView передаётся как слот ===
        mapView = {
            IosMapView(
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
                            ?: return@IosMapView
                        mapViewModel.onMarkerSelected(marker)
                    }
                },
                onMapClick = {
                    mapViewModel.clearSelectedMarker()
                }
            )
        },

        // === Callbacks (в т.ч. зум) ===
        onZoomIn = { mapController.value?.zoomIn() },
        onZoomOut = { mapController.value?.zoomOut() },
        onMyLocation = {
            pendingCenterOnMyLocation = true
            mapViewModel.updateCurrentLocation()

            currentLocation?.let {
                mapController.value?.moveTo(it.lat, it.lon, 12.0)
                pendingCenterOnMyLocation = false
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
