package com.saionji.mysensor.shared.fake

import com.saionji.mysensor.shared.data.model.SettingsApp
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    initialSettings: List<SettingsSensor> = emptyList(),
    initialAppSettings: SettingsApp = SettingsApp(true)
) : SettingsRepository {

    private val _settings = MutableStateFlow(initialSettings)
    private val _appSettings = MutableStateFlow(initialAppSettings)

    // Для проверки вызовов в тестах
    var saveSettingsCallCount = 0
        private set
    var lastSavedSettings: List<SettingsSensor>? = null
        private set

    override suspend fun saveSettings(sensors: List<SettingsSensor>) {
        saveSettingsCallCount++
        lastSavedSettings = sensors
        _settings.value = sensors
    }

    override suspend fun saveAppSettings(settings: SettingsApp) {
        _appSettings.value = settings
    }

    override fun getSettings(): Flow<List<SettingsSensor>> = _settings

    override fun getAppSettings(): Flow<SettingsApp> = _appSettings

    // Helper для тестов
    fun emitSettings(settings: List<SettingsSensor>) {
        _settings.value = settings
    }
}