package com.saionji.mysensor.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.saionji.mysensor.C
import com.saionji.mysensor.MySensorApplication
import com.saionji.mysensor.domain.model.LatLng
import com.saionji.mysensor.data.MySensor
import com.saionji.mysensor.data.SettingsSensor
import com.saionji.mysensor.domain.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.domain.GetSensorValuesUseCase
import com.saionji.mysensor.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.domain.model.LatLngBounds
import com.saionji.mysensor.domain.model.MapBounds
import com.saionji.mysensor.network.model.MySensorRawData
import com.saionji.mysensor.ui.map.model.MapMarkerUiModel
import com.saionji.mysensor.ui.map.model.SelectedMarkerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

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

    private val _mapUiState = MutableStateFlow<MapUiState>(MapUiState.Idle)
    val mapUiState: StateFlow<MapUiState> = _mapUiState

    private val _currentLocation = mutableStateOf<LatLng?>(null)
    val currentLocation: State<LatLng?> = _currentLocation

    private val addressCache = mutableMapOf<String, String>()

    private val _addresses =
        MutableStateFlow<Map<String, String>>(emptyMap())

    private fun addressKey(lat: Double, lon: Double) = "$lat,$lon"

    val addresses: StateFlow<Map<String, String>> = _addresses

    private val _selectedValueType = MutableStateFlow("PM2.5")
    val selectedValueType: StateFlow<String> = _selectedValueType

    private var lastMapSensors: List<MySensorRawData> = emptyList()

    fun loadAddressIfNeeded(
        lat: Double,
        lon: Double,
        onResult: (String) -> Unit
    ) {
        val key = "$lat,$lon"

        addressCache[key]?.let {
            onResult(it)
            return
        }

        viewModelScope.launch {
            val address = getAddressFromCoordinatesUseCase(lat, lon)
            addressCache[key] = address
            onResult(address)
        }
    }

    fun ensureAddress(lat: Double, lon: Double) {
        val key = addressKey(lat, lon)

        if (_addresses.value.containsKey(key)) return

        viewModelScope.launch {
            val address = getAddressFromCoordinatesUseCase(lat, lon)
            _addresses.update { it + (key to address) }
        }
    }

    private fun mapToUiModels(
        sensors: List<MySensorRawData>,
        valueType: String
    ): List<MapMarkerUiModel> {

        return sensors.mapNotNull { sensor ->

            val value = sensor.sensordatavalues
                .find { it.valueType == valueType }
                ?.value
                ?: return@mapNotNull null

            val colorInt = MarkerColorResolver.resolveColorInt(
                valueType = valueType,
                value = value
            )

            MapMarkerUiModel(
                id = sensor.sensor?.id ?: return@mapNotNull null,
                lat = sensor.location.latitude,
                lon = sensor.location.longitude,
                valueType = valueType,
                value = value,
                colorInt = MarkerColorResolver.resolveColorInt(
                    valueType,
                    value
                )
            )
        }
    }

    fun loadSensorsForArea(bounds: MapBounds) {
        val valueType = _selectedValueType.value

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
            it.sensor?.id.toString() == sensorId
        } ?: return

        val deviceSensors = rawSensor.sensordatavalues.mapNotNull { data ->
            val type = data.valueType ?: return@mapNotNull null

            MySensor(
                valueType = type,
                value = data.value.toString(),
                color = data.color
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
                _currentLocation.value = LatLng(
                    lat = location.latitude,
                    lon = location.longitude
                )
            }
        }.addOnFailureListener {
            Log.e("Location", "Ошибка при получении текущего местоположения", it)
        }
    }

    suspend fun getAddressFromCoordinates(context: Context, lat: Double, lon: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    listOfNotNull(
                        address.thoroughfare, // улица
                        address.subThoroughfare, // номер дома
                        address.locality, // город
                        address.countryName // страна
                    ).joinToString(", ")
                } else {
                    "Unknown address"
                }
            } catch (e: Exception) {
                "Error getting address"
            }
        }
    }

    fun setSelectedValueType(type: String) {
        _selectedValueType.value = type
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