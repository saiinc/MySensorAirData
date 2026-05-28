package com.saionji.mysensor.shared.ui.map

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.domain.model.LatLng
import com.saionji.mysensor.shared.domain.model.MapMarker
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.shared.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.shared.ui.map.model.MapBounds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val VIEWPORT_DEBOUNCE_MS = 700L
private const val MIN_RELOAD_ZOOM_DELTA = 0.25
private const val MIN_RELOAD_VIEWPORT_SHIFT_RATIO = 0.25

@OptIn(FlowPreview::class)
class SharedMapViewModel(
    private val getAddressFromCoordinatesUseCase: GetAddressFromCoordinatesUseCase,
    private val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase,
    private val locationService: LocationService,
    private val scope: CoroutineScope
) {
    data class CameraState(
        val lat: Double,
        val lon: Double,
        val zoom: Double,
        val isProgrammatic: Boolean = false
    )
    private val _cameraState = MutableStateFlow<CameraState?>(null)
    val cameraState: StateFlow<CameraState?> = _cameraState

    private var initialCameraApplied = false

    private val _mapUiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val mapUiState: StateFlow<MapUiState> = _mapUiState

    private val _currentLocation = mutableStateOf<LatLng?>(null)
    val currentLocation: State<LatLng?> = _currentLocation

    private val _addresses =
        MutableStateFlow<Map<String, String>>(emptyMap())
    val addresses: StateFlow<Map<String, String>> = _addresses

    private fun addressKey(lat: Double?, lon: Double?) = "$lat,$lon"

    private val _selectedValueType = MutableStateFlow("PM2.5")
    val selectedValueType: StateFlow<String> = _selectedValueType

    private var lastBounds: MapBounds? = null
    private var lastLoadedBounds: MapBounds? = null
    private var lastLoadedValueType: String? = null
    private var loadJob: Job? = null

    private val _selectedMarker =
        MutableStateFlow<MapMarker?>(null)

    val selectedMarker: StateFlow<MapMarker?> =
        _selectedMarker

    private val viewportFlow = MutableSharedFlow<MapBounds>(
        extraBufferCapacity = 1
    )

    fun ensureAddress(lat: Double?, lon: Double?) {
        val key = addressKey(lat, lon)

        if (_addresses.value.containsKey(key)) return

        scope.launch {
            val address = getAddressFromCoordinatesUseCase(lat, lon)
            _addresses.update { it + (key to address) }
        }
    }

    fun onMarkerSelected(marker: MapMarker) {
        _selectedMarker.value = marker
        ensureAddress(marker.lat, marker.lon)
    }

    fun clearSelectedMarker() {
        _selectedMarker.value = null
    }

    fun onViewportChanged(bounds: MapBounds) {
        viewportFlow.tryEmit(bounds)
    }

    fun onLocationUpdated(lat: Double, lon: Double) {
        _currentLocation.value = LatLng(lat, lon)

        if (!initialCameraApplied) {
            _cameraState.value = CameraState(
                lat,
                lon,
                zoom = 10.5,
                isProgrammatic = true
            )
            initialCameraApplied = true
        }
    }

    fun onCameraMovedFromUser(bounds: MapBounds) {
        _cameraState.value = CameraState(
            lat = (bounds.north + bounds.south) / 2,
            lon = (bounds.east + bounds.west) / 2,
            zoom = bounds.zoom,
            isProgrammatic = false
        )
    }

    init {
        scope.launch {
            viewportFlow
                .debounce(VIEWPORT_DEBOUNCE_MS)
                .collect { bounds ->
                    loadSensorsForArea(bounds)
                }
        }
    }

    fun loadSensorsForArea(bounds: MapBounds) {
        lastBounds = bounds
        if (bounds.zoom < 6.0) {
            loadJob?.cancel()
            _mapUiState.value = MapUiState.Idle
            return
        }
        if (bounds.north - bounds.south > 50 ||
            bounds.east - bounds.west > 50
        ) {
            return
        }

        val valueType = _selectedValueType.value
        if (isSameLoadedViewport(bounds, valueType)) return

        loadJob?.cancel()
        loadJob = scope.launch {
            if (_mapUiState.value !is MapUiState.Success) {
                _mapUiState.value = MapUiState.Loading
            }
            try {
                val markers = getSensorValuesByAreaUseCase(
                    bounds.north,
                    bounds.west,
                    bounds.south,
                    bounds.east,
                    valueType
                )

                lastLoadedBounds = bounds
                lastLoadedValueType = valueType
                _mapUiState.value = MapUiState.Success(markers)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mapUiState.value = MapUiState.Error(
                    e.message ?: "Ошибка загрузки данных"
                )
            }
        }
    }

    fun buildSettingsSensorFromMap(
        sensorId: String,
        address: String,
        onResult: (SettingsSensor) -> Unit
    ) {
        val settingsSensor = SettingsSensor(
            id = sensorId,
            description = address
        )
        onResult(settingsSensor)
    }

    fun updateCurrentLocation() {
        scope.launch(Dispatchers.Main) {
            locationService.getCurrentLocation { latLng ->
                if (latLng != null) {
                    onLocationUpdated(latLng.lat, latLng.lon)
                }
            }
        }
    }

    fun setSelectedValueType(type: String) {
        if (_selectedValueType.value == type) return

        _selectedValueType.value = type

        lastBounds?.let {
            viewportFlow.tryEmit(it)
        }
    }

    private fun isSameLoadedViewport(bounds: MapBounds, valueType: String): Boolean {
        val loadedBounds = lastLoadedBounds ?: return false
        if (lastLoadedValueType != valueType) return false

        if (abs(bounds.zoom - loadedBounds.zoom) >= MIN_RELOAD_ZOOM_DELTA) {
            return false
        }

        val latSpan = abs(loadedBounds.north - loadedBounds.south)
        val lonSpan = abs(loadedBounds.east - loadedBounds.west)
        val latShift = abs(bounds.centerLat - loadedBounds.centerLat)
        val lonShift = abs(bounds.centerLon - loadedBounds.centerLon)

        return latShift < latSpan * MIN_RELOAD_VIEWPORT_SHIFT_RATIO &&
            lonShift < lonSpan * MIN_RELOAD_VIEWPORT_SHIFT_RATIO
    }

    private val MapBounds.centerLat: Double
        get() = (north + south) / 2

    private val MapBounds.centerLon: Double
        get() = (east + west) / 2
}
