package com.saionji.mysensor.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saionji.mysensor.MySensorApplication
import com.saionji.mysensor.shared.di.SharedContainer
import com.saionji.mysensor.shared.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.shared.ui.map.AndroidLocationService
import com.saionji.mysensor.shared.ui.map.LocationService
import com.saionji.mysensor.shared.ui.map.SharedMapViewModel

/**
 * Android wrapper for SharedMapViewModel
 *
 * Handles Android-specific concerns:
 * - Lifecycle management (ViewModel)
 * - Factory for dependency injection
 * - Context for LocationService
 */
class AndroidMapViewModel(
    private val getAddressFromCoordinatesUseCase: GetAddressFromCoordinatesUseCase,
    private val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase,
    private val locationService: LocationService
) : ViewModel() {

    /**
     * Shared ViewModel с бизнес-логикой
     * ✅ viewModelScope доступен ВНУТРИ конструктора AndroidMapViewModel!
     */
    private val sharedViewModel = SharedMapViewModel(
        getAddressFromCoordinatesUseCase = getAddressFromCoordinatesUseCase,
        getSensorValuesByAreaUseCase = getSensorValuesByAreaUseCase,
        locationService = locationService,
        scope = viewModelScope
    )

    // ==================== STATE FLOWS ====================

    val cameraState = sharedViewModel.cameraState
    val mapUiState = sharedViewModel.mapUiState
    val currentLocation = sharedViewModel.currentLocation
    val addresses = sharedViewModel.addresses
    val selectedValueType = sharedViewModel.selectedValueType
    val selectedMarker = sharedViewModel.selectedMarker

    // ==================== METHODS PROXY ====================

    fun onMarkerSelected(marker: com.saionji.mysensor.shared.domain.model.MapMarker) = sharedViewModel.onMarkerSelected(marker)
    fun clearSelectedMarker() = sharedViewModel.clearSelectedMarker()
    fun onViewportChanged(bounds: com.saionji.mysensor.shared.ui.map.model.MapBounds) = sharedViewModel.onViewportChanged(bounds)
    fun onCameraMovedFromUser(bounds: com.saionji.mysensor.shared.ui.map.model.MapBounds) = sharedViewModel.onCameraMovedFromUser(bounds)
    fun buildSettingsSensorFromMap(sensorId: String, address: String, onResult: (com.saionji.mysensor.shared.data.model.SettingsSensor) -> Unit) = sharedViewModel.buildSettingsSensorFromMap(sensorId, address, onResult)
    fun updateCurrentLocation() = sharedViewModel.updateCurrentLocation()
    fun setSelectedValueType(type: String) = sharedViewModel.setSelectedValueType(type)

    // ==================== FACTORY ====================

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MySensorApplication)
                val sharedContainer = application.container as SharedContainer

                val locationService = AndroidLocationService(application.applicationContext)

                AndroidMapViewModel(
                    getAddressFromCoordinatesUseCase = sharedContainer.getAddressFromCoordinatesUseCase,
                    getSensorValuesByAreaUseCase = sharedContainer.getSensorValuesByAreaUseCase,
                    locationService = locationService
                )
            }
        }
    }
}