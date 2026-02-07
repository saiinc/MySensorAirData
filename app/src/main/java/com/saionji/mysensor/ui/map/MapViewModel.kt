package com.saionji.mysensor.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.saionji.mysensor.MySensorApplication
import com.saionji.mysensor.data.MapSensor
import com.saionji.mysensor.domain.model.LatLng
import com.saionji.mysensor.data.MySensor
import com.saionji.mysensor.data.SettingsSensor
import com.saionji.mysensor.domain.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.ui.map.model.MapBounds
import com.saionji.mysensor.ui.map.model.MapMarkerUiModel
import com.saionji.mysensor.ui.map.model.SelectedMarkerUi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class MapViewModel(
    private val getAddressFromCoordinatesUseCase: GetAddressFromCoordinatesUseCase,
    private val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase
) : ViewModel() {

    sealed interface MapUiState {
        object Idle : MapUiState
        object Loading : MapUiState
        data class Success(val markers: List<MapMarkerUiModel>) : MapUiState
        data class Error(val message: String) : MapUiState
    }

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

    private fun addressKey(lat: Double?, lon: Double?) = "$lat,$lon"

    val addresses: StateFlow<Map<String, String>> = _addresses

    private val _selectedValueType = MutableStateFlow("PM2.5")
    val selectedValueType: StateFlow<String> = _selectedValueType

    private var lastMapSensors: List<MapSensor> = emptyList()

    private val _selectedMarker = MutableStateFlow<SelectedMarkerUi?>(null)
    val selectedMarker: StateFlow<SelectedMarkerUi?> = _selectedMarker

    private val viewportFlow = MutableSharedFlow<MapBounds>(
        extraBufferCapacity = 1
    )

    fun ensureAddress(lat: Double?, lon: Double?) {
        val key = addressKey(lat, lon)

        if (_addresses.value.containsKey(key)) return

        viewModelScope.launch {
            val address = getAddressFromCoordinatesUseCase(lat, lon)
            _addresses.update { it + (key to address) }
        }
    }

    private fun mapToUiModels(
        sensors: List<MapSensor>,
        valueType: String
    ): List<MapMarkerUiModel> {

        return sensors.mapNotNull { sensor ->

            val value = sensor.measurements
                .find { it.valueType == valueType }
                ?.value
                ?: return@mapNotNull null

            MapMarkerUiModel(
                id = sensor.id,
                lat = sensor.lat,
                lon = sensor.lon,
                valueType = valueType,
                value = value,
                colorInt = MarkerColorResolver.resolveColorInt(
                    valueType,
                    value
                )
            )
        }
    }

    fun onMarkerSelected(marker: SelectedMarkerUi) {
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

    fun saveCameraState(lat: Double, lon: Double, zoom: Double) {
        _cameraState.value = CameraState(lat, lon, zoom)
    }

    init {
        viewModelScope.launch {
            viewportFlow
                .debounce(600)
                .collect { bounds ->
                    loadSensorsForArea(bounds)
                }
        }
    }

    fun loadSensorsForArea(bounds: MapBounds) {
        val valueType = _selectedValueType.value
        if (bounds.zoom < 6.0) {
            _mapUiState.value = MapUiState.Idle
            return
        }

        if (bounds.north - bounds.south > 40 ||
            bounds.east - bounds.west > 40
        ) {
            return
        }

        viewModelScope.launch {
            _mapUiState.value = MapUiState.Loading
            try {
                val sensors = getSensorValuesByAreaUseCase(
                    bounds.north,
                    bounds.west,
                    bounds.south,
                    bounds.east
                )

                lastMapSensors = sensors

                val markers = mapToUiModels(sensors, valueType)
                _mapUiState.value = MapUiState.Success(markers)

                _mapUiState.value = MapUiState.Success(markers)
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
        val rawSensor = lastMapSensors.firstOrNull {
            it.id == sensorId
        } ?: return

        val deviceSensors = rawSensor.measurements.map { data ->
            val type = data.valueType

            MySensor(
                valueType = type,
                value = data.value.toString(),
                color = Color.Transparent
            )
        }

        val settingsSensor = SettingsSensor(
            id = sensorId,
            description = address,
            deviceSensors = deviceSensors
        )

        onResult(settingsSensor)
    }

    @SuppressLint("MissingPermission")
    fun updateCurrentLocation(context: Context) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                onLocationUpdated(
                    lat = location.latitude,
                    lon = location.longitude
                )
            }
        }.addOnFailureListener {
            Log.e("Location", "Ошибка при получении текущего местоположения", it)
        }
    }

    fun setSelectedValueType(type: String) {
        _selectedValueType.value = type

        if (lastMapSensors.isNotEmpty()) {
            val markers = mapToUiModels(lastMapSensors, type)
            _mapUiState.value = MapUiState.Success(markers)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MySensorApplication)
                val container = application.container
                MapViewModel(
                    getSensorValuesByAreaUseCase = container.getSensorValuesByAreaUseCase,
                    getAddressFromCoordinatesUseCase = container.getAddressFromCoordinatesUseCase
                )
            }
        }
    }
}