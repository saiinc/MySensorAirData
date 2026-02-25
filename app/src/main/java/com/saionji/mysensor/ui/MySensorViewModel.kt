/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saionji.mysensor.MySensorApplication
import com.saionji.mysensor.shared.data.model.DashboardSensor
import com.saionji.mysensor.shared.data.model.SettingsApp
import com.saionji.mysensor.data.SettingsRepository
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException


sealed class Screen {
    object Dashboard : Screen()
    object Map : Screen()
}

class MySensorViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository,
    private val getSensorValuesUseCase: GetSensorValuesUseCase,
) : AndroidViewModel(application) {

    private val _optionsBoxState: MutableState<OptionsBoxState> =
        mutableStateOf(value = OptionsBoxState.CLOSED)
    val optionsBoxState: State<OptionsBoxState> = _optionsBoxState

    private val dashboardThrottle = ThrottleExecutor(delayMillis = 5_000)

    private fun onDashboardOpened() {
        viewModelScope.launch {
            dashboardThrottle.run {
                getDeviceSensors()
            }
        }
    }

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen

    fun switchToScreen(screen: Screen) {
        _currentScreen.value = screen
        if (screen == Screen.Dashboard) {
            onDashboardOpened()
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    fun refresh() = viewModelScope.launch {
        _isRefreshing.update { true }
        getDeviceSensors()
        delay(1500)
        _isRefreshing.update { false }
    }

    enum class ErrorType {
        Network,
        Unknown
    }

    private val _error = MutableStateFlow<ErrorType?>(null)
    val error: StateFlow<ErrorType?> = _error

    fun showError(type: ErrorType) {
        _error.value = type
    }

    fun clearError() {
        _error.value = null
    }

    private val _sensorIdTextState: MutableState<String> =
        mutableStateOf(value = "")
    val sensorIdTextState: State<String> = _sensorIdTextState

    private val _settingsApp: MutableState<SettingsApp> =
        mutableStateOf(value = SettingsApp(true))
    val settingsApp: State<SettingsApp> = _settingsApp

    private val _dashboardItems =
        MutableStateFlow<List<DashboardSensor>>(emptyList())
    val dashboardItems: StateFlow<List<DashboardSensor>> = _dashboardItems

    private val _sensorsOptions = MutableStateFlow<List<SettingsSensor>>(emptyList())
    val sensorsOptions: StateFlow<List<SettingsSensor>> get() = _sensorsOptions

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun navigateTo(screen: String) {
        viewModelScope.launch {
            _navigationEvent.emit(screen)
        }
    }

    private val _showShareScreen = mutableStateOf(false)
    val showShareScreen: State<Boolean> = _showShareScreen

    fun setShowShareScreen(value: Boolean) {
        _showShareScreen.value = value
    }

    fun updateSettingsAppState(newValue: SettingsApp) {
        _settingsApp.value = newValue
    }

    fun updateOptionsBoxState(newValue: OptionsBoxState) {
        _optionsBoxState.value = newValue
    }
    fun updateSensorIdTextState(newValue: String) {
        _sensorIdTextState.value = newValue
    }

    fun saveAppSettings(settingsApp: SettingsApp) {
        viewModelScope.launch {
            settingsRepository.saveAppSettings(settingsApp)
        }
    }

    fun saveSensors(sensors: List<SettingsSensor>) {
        viewModelScope.launch {
            settingsRepository.saveSettings(sensors)
        }
    }

    fun sensorsOptionsLoad() {
        if (dashboardItems.value.isEmpty()) {
            _sensorsOptions.value = listOf(SettingsSensor("", ""))
        } else {
            _sensorsOptions.value = dashboardItems.value.map {
                SettingsSensor(
                    id = it.id,
                    description = it.description
                )
            }
        }
    }

    fun addSensorDashboardFromMap(settingsSensor: SettingsSensor) {
        val fromMapItem = DashboardSensor(
            id = settingsSensor.id,
            description = settingsSensor.description,
            deviceSensors = emptyList()
        )
        _dashboardItems.value += fromMapItem
        saveSensors(
            dashboardItems.value.map {
                SettingsSensor(
                    id = it.id,
                    description = it.description
                )
            }
        )
    }

    fun removeSensorDashboardFromMap(id: String) {
        _dashboardItems.value.find { it.id == id }?.let { foundItem ->
            _dashboardItems.value -= foundItem
        }
        saveSensors(
            dashboardItems.value.map {
                SettingsSensor(
                    id = it.id,
                    description = it.description
                )
            }
        )
    }

    fun addSensorOptions() {
        _sensorsOptions.value += SettingsSensor(id = "", description = "")
    }

    fun removeSensorOptions(settingsSensorItem: SettingsSensor) {
        _sensorsOptions.value -= settingsSensorItem
    }

    fun updateSensorOptionsItemId(index: Int, settingsSensorItemId: String) {
        _sensorsOptions.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) item.copy(id = settingsSensorItemId) else item
            }
        }
    }

    fun updateSensorOptionsItemDescription(index: Int, settingsSensorItemDescription: String) {
        _sensorsOptions.update { list ->
            list.mapIndexed { i, item ->
                if (i == index) item.copy(description = settingsSensorItemDescription) else item
            }
        }
    }

    init {
        initLoad()
    }
    private fun initLoad() {
        viewModelScope.launch {
            settingsRepository.getSettings().collectLatest { savedSettings ->

                if (savedSettings.isEmpty()) return@collectLatest

                // создаём runtime-объекты без сенсоров
                val dashboard = savedSettings.map {
                    DashboardSensor(
                        id = it.id,
                        description = it.description,
                        deviceSensors = emptyList(),
                        isLoading = true
                    )
                }

                _dashboardItems.value = dashboard

                // загружаем данные один раз
                getDeviceSensors()
            }
        }

        viewModelScope.launch {
            settingsRepository.getAppSettings().collectLatest {
                _settingsApp.value = it
            }
        }
    }

    fun resetAppSettings() {
        viewModelScope.launch(Dispatchers.Main) {
            settingsRepository.getAppSettings().collectLatest {
                _settingsApp.value = it
            }
        }
    }

    fun getDeviceSensors() {
        viewModelScope.launch {

            val current = _dashboardItems.value

            if (current.isEmpty()) return@launch

            current.forEach { item ->

                launch {
                    try {
                        val updatedSensors = getSensorValuesUseCase(
                            SettingsSensor(item.id, item.description)
                        )

                        _dashboardItems.update { list ->
                            list.map { existing ->
                                if (existing.id == item.id) {
                                    existing.copy(
                                        deviceSensors = updatedSensors,
                                        isLoading = false)
                                } else existing
                            }
                        }
                        clearError()

                    } catch (_: IOException) {

                        showError(ErrorType.Network)

                        _dashboardItems.update { list ->
                            list.map { existing ->
                                if (existing.id == item.id) {
                                    existing.copy(
                                        deviceSensors = existing.deviceSensors.map {
                                            it.copy(value = "—")
                                        },
                                        isLoading = false
                                    )
                                } else existing
                            }
                        }
                    }
                }
            }
        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MySensorApplication)
                val settingsRepository = application.container.settingsRepository
                val getSensorValuesUseCase = application.container.getSensorValuesUseCase

                MySensorViewModel(
                    settingsRepository = settingsRepository,
                    getSensorValuesUseCase = getSensorValuesUseCase,
                    application = application
                )
            }
        }
    }
}

enum class OptionsBoxState {
    OPENED,
    CLOSED
}