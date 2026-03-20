package com.saionji.mysensor.shared.ui.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.saionji.mysensor.shared.data.model.DashboardSensor
import com.saionji.mysensor.shared.data.model.SettingsApp
import com.saionji.mysensor.shared.data.repository.SettingsRepository
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.saionji.mysensor.shared.ui.components.OptionsBoxState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.distinctUntilChanged


sealed class Screen {
    object Dashboard : Screen()
    object Map : Screen()
}

class MySensorViewModel(
    private val settingsRepository: SettingsRepository,
    private val getSensorValuesUseCase: GetSensorValuesUseCase,
    private val scope: CoroutineScope  // ✅ Передается извне
) : ViewModel() {

    private val _optionsBoxState: MutableState<OptionsBoxState> =
        mutableStateOf(value = OptionsBoxState.CLOSED)
    val optionsBoxState: State<OptionsBoxState> = _optionsBoxState

    private val dashboardThrottle = ThrottleExecutor(delayMillis = 5_000)

    private fun onDashboardOpened() {
        scope.launch {
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

    fun refresh() = scope.launch {
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
        scope.launch {
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
        scope.launch {
            settingsRepository.saveAppSettings(settingsApp)
        }
    }

    fun saveSensors(sensors: List<SettingsSensor>) {
        scope.launch {
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
        scope.launch {
            settingsRepository.getSettings()
                .distinctUntilChanged()
                .collectLatest { savedSettings ->

                    if (savedSettings.isEmpty()) return@collectLatest

                    val dashboard = savedSettings.map {
                        DashboardSensor(
                            id = it.id,
                            description = it.description,
                            deviceSensors = emptyList(),
                            isLoading = true
                        )
                    }

                    if (_dashboardItems.value.map { it.id } != dashboard.map { it.id }) {
                        _dashboardItems.value = dashboard
                        getDeviceSensors()
                    }
                }
        }

        scope.launch {
            settingsRepository.getAppSettings().collectLatest {
                _settingsApp.value = it
            }
        }
    }

    fun resetAppSettings() {
        scope.launch {
            settingsRepository.getAppSettings().collectLatest {
                _settingsApp.value = it
            }
        }
    }

    fun getDeviceSensors() {
        scope.launch {

            val current = _dashboardItems.value
            if (current.isEmpty()) return@launch

            val results = current.map { item ->

                async {
                    try {

                        val sensors = getSensorValuesUseCase(
                            SettingsSensor(item.id, item.description)
                        )

                        item.copy(
                            deviceSensors = sensors,
                            isLoading = false
                        ) to false

                    } catch (e: Exception) {

                        item.copy(
                            deviceSensors = item.deviceSensors.map {
                                it.copy(value = "—")
                            },
                            isLoading = false
                        ) to true
                    }
                }

            }.awaitAll()

            _dashboardItems.value = results.map { it.first }

            val hasError = results.any { it.second }

            if (hasError) {
                showError(ErrorType.Network)
            } else {
                clearError()
            }
        }
    }
}