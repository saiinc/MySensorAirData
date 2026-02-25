package com.saionji.mysensor.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
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
import com.saionji.mysensor.shared.domain.model.LatLng
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.shared.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.shared.domain.model.MapMarker
import com.saionji.mysensor.ui.map.model.MapBounds
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
        data class Success(val markers: List<MapMarker>) : MapUiState
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
    val addresses: StateFlow<Map<String, String>> = _addresses

    private fun addressKey(lat: Double?, lon: Double?) = "$lat,$lon"

    private val _selectedValueType = MutableStateFlow("PM2.5")
    val selectedValueType: StateFlow<String> = _selectedValueType

    private var lastBounds: MapBounds? = null

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

        viewModelScope.launch {
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
        viewModelScope.launch {
            viewportFlow
                .debounce(600)
                .collect { bounds ->
                    loadSensorsForArea(bounds)
                }
        }
    }

    fun loadSensorsForArea(bounds: MapBounds) {
        lastBounds = bounds
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
                val markers = getSensorValuesByAreaUseCase(
                    bounds.north,
                    bounds.west,
                    bounds.south,
                    bounds.east,
                    _selectedValueType.value
                )

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

        val settingsSensor = SettingsSensor(
            id = sensorId,
            description = address

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

        if (_selectedValueType.value == type) return

        _selectedValueType.value = type

        lastBounds?.let {
            viewportFlow.tryEmit(it)
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