/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saionji.mysensor.MySensorApplication
import com.saionji.mysensor.data.SettingsApp
import com.saionji.mysensor.data.SettingsRepository
import com.saionji.mysensor.data.SettingsSensor
import com.saionji.mysensor.domain.GetSensorValuesUseCase
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

    private val _showErrorMessage = MutableStateFlow(false)
    val showErrorMessage: StateFlow<Boolean> = _showErrorMessage

    fun setShowErrorMessage(value: Boolean) {
        _showErrorMessage.value = value
    }

    private val _sensorIdTextState: MutableState<String> =
        mutableStateOf(value = "")
    val sensorIdTextState: State<String> = _sensorIdTextState

    private val _settingsApp: MutableState<SettingsApp> =
        mutableStateOf(value = SettingsApp(true))
    val settingsApp: State<SettingsApp> = _settingsApp

    private val _settingsItems = MutableStateFlow<List<SettingsSensor>>(emptyList())
    val settingsItems: StateFlow<List<SettingsSensor>> get() = _settingsItems

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

    fun sensorsLoad(sensors: List<SettingsSensor>) {
        _settingsItems.value = sensors
        _sensorsOptions.value = emptyList()
    }

    fun sensorsOptionsLoad() {
        if (_settingsItems.value.isEmpty()) {
            _sensorsOptions.value = listOf(SettingsSensor("", "", emptyList()))
        } else {
            _sensorsOptions.value = _settingsItems.value
        }
    }

    fun addSensorDashboardFromMap(settingsSensor: SettingsSensor) {
        _settingsItems.value += settingsSensor
    }

    fun removeSensorDashboardFromMap(id: String) {
        _settingsItems.value.find { it.id == id }?.let { foundItem ->
            _settingsItems.value -= foundItem
        }
    }

    fun addSensorOptions() {
        _sensorsOptions.value += SettingsSensor(id = "", description = "", emptyList())
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
        viewModelScope.launch(Dispatchers.Main) {
            settingsRepository.getSettings().collectLatest { mySettings ->
                if (mySettings.isNotEmpty())
                    _settingsItems.value = mySettings
                getDeviceSensors()
            }
        }
        viewModelScope.launch(Dispatchers.Main) {
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
            if (_settingsItems.value.isNotEmpty()) {
                _settingsItems.value.forEach { item ->
                    launch {
                        try {
                            val updatedSensors = getSensorValuesUseCase(item)
                            _settingsItems.update { currentList ->
                                currentList.map { currentItem ->
                                    if (currentItem.id == item.id) {
                                        currentItem.copy(deviceSensors = updatedSensors)
                                    } else {
                                        currentItem
                                    }
                                }
                            }
                        } catch (_: IOException) {
                            setShowErrorMessage(true)
                            _settingsItems.update { currentList ->
                                currentList.map { currentItem ->
                                    if (currentItem.id == item.id) {
                                        currentItem.copy(deviceSensors = item.deviceSensors.map { sensorItem ->
                                            sensorItem.copy(
                                                value = "—",
                                                valueType = sensorItem.valueType
                                            )
                                        }
                                        )
                                    } else {
                                        currentItem
                                    }
                                }
                            }
                        }
                    }
                }
                delay(5000)
                saveSensors(settingsItems.value.map { it.copy() })
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