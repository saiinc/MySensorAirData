/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saionji.mysensor.MySensorApplication
import com.saionji.mysensor.shared.data.model.SettingsApp
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.ui.viewmodel.MySensorViewModel as SharedMySensorViewModel
import com.saionji.mysensor.shared.data.repository.SettingsRepository
import com.saionji.mysensor.shared.di.SharedContainer
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesUseCase
import com.saionji.mysensor.shared.ui.components.OptionsBoxState
import com.saionji.mysensor.shared.ui.viewmodel.Screen

/**
 * Android wrapper для Shared ViewModel
 *
 * Обеспечивает:
 * - Android Lifecycle (через наследование ViewModel)
 * - viewModelScope (передается в shared ViewModel)
 * - Совместимость с Android DI
 */
class AndroidMySensorViewModel(
    settingsRepository: SettingsRepository,
    getSensorValuesUseCase: GetSensorValuesUseCase
) : ViewModel() {

    /**
     * Shared ViewModel с бизнес-логикой
     */
    private val sharedViewModel = SharedMySensorViewModel(
        settingsRepository = settingsRepository,
        getSensorValuesUseCase = getSensorValuesUseCase,
        scope = viewModelScope  // ✅ Передаем Android viewModelScope
    )

    // ==================== ПРОКСИРОВАНИЕ СВОЙСТВ ====================

    val optionsBoxState = sharedViewModel.optionsBoxState
    val currentScreen = sharedViewModel.currentScreen
    val isRefreshing = sharedViewModel.isRefreshing
    val error = sharedViewModel.error
    val sensorIdTextState = sharedViewModel.sensorIdTextState
    val settingsApp = sharedViewModel.settingsApp
    val dashboardItems = sharedViewModel.dashboardItems
    val sensorsOptions = sharedViewModel.sensorsOptions
    val navigationEvent = sharedViewModel.navigationEvent
    val showShareScreen = sharedViewModel.showShareScreen

    // ==================== ПРОКСИРОВАНИЕ МЕТОДОВ ====================

    fun switchToScreen(screen: Screen) = sharedViewModel.switchToScreen(screen)
    fun refresh() = sharedViewModel.refresh()
    fun showError(type: SharedMySensorViewModel.ErrorType) = sharedViewModel.showError(type)
    fun clearError() = sharedViewModel.clearError()
    fun navigateTo(screen: String) = sharedViewModel.navigateTo(screen)
    fun setShowShareScreen(value: Boolean) = sharedViewModel.setShowShareScreen(value)
    fun updateSettingsAppState(newValue: SettingsApp) = sharedViewModel.updateSettingsAppState(newValue)
    fun updateOptionsBoxState(newValue: OptionsBoxState) = sharedViewModel.updateOptionsBoxState(newValue)
    fun updateSensorIdTextState(newValue: String) = sharedViewModel.updateSensorIdTextState(newValue)
    fun saveAppSettings(settingsApp: SettingsApp) = sharedViewModel.saveAppSettings(settingsApp)
    fun saveSensors(sensors: List<SettingsSensor>) = sharedViewModel.saveSensors(sensors)
    fun sensorsOptionsLoad() = sharedViewModel.sensorsOptionsLoad()
    fun addSensorDashboardFromMap(settingsSensor: SettingsSensor) = sharedViewModel.addSensorDashboardFromMap(settingsSensor)
    fun removeSensorDashboardFromMap(id: String) = sharedViewModel.removeSensorDashboardFromMap(id)
    fun addSensorOptions() = sharedViewModel.addSensorOptions()
    fun removeSensorOptions(settingsSensorItem: SettingsSensor) = sharedViewModel.removeSensorOptions(settingsSensorItem)
    fun updateSensorOptionsItemId(index: Int, settingsSensorItemId: String) = sharedViewModel.updateSensorOptionsItemId(index, settingsSensorItemId)
    fun updateSensorOptionsItemDescription(index: Int, settingsSensorItemDescription: String) = sharedViewModel.updateSensorOptionsItemDescription(index, settingsSensorItemDescription)
    fun resetAppSettings() = sharedViewModel.resetAppSettings()
    fun getDeviceSensors() = sharedViewModel.getDeviceSensors()

    // ==================== FACTORY ====================

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MySensorApplication)
                val sharedContainer = application.container as SharedContainer

                AndroidMySensorViewModel(
                    settingsRepository = sharedContainer.settingsRepository,
                    getSensorValuesUseCase = sharedContainer.getSensorValuesUseCase
                )
            }
        }
    }
}