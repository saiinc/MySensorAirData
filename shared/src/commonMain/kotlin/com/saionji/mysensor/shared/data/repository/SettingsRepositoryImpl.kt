package com.saionji.mysensor.shared.data.repository

import com.russhwolf.settings.Settings
import com.saionji.mysensor.shared.data.model.SettingsApp
import com.saionji.mysensor.shared.data.model.SettingsSensor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepositoryImpl(
    private val settings: Settings
) : SettingsRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private companion object {
        const val APP_SETTINGS_KEY = "app_settings"
        const val SENSORS_KEY = "my_objects_key"
    }

    private val _appSettingsFlow = MutableStateFlow(loadAppSettings())
    private val _sensorsFlow = MutableStateFlow(loadSensors())

    override suspend fun saveAppSettings(settingsApp: SettingsApp) {
        settings.putString(APP_SETTINGS_KEY, json.encodeToString(settingsApp))
        _appSettingsFlow.value = settingsApp
    }

    override suspend fun saveSettings(sensors: List<SettingsSensor>) {
        settings.putString(SENSORS_KEY, json.encodeToString(sensors))
        _sensorsFlow.value = sensors
    }

    override fun getAppSettings(): Flow<SettingsApp> = _appSettingsFlow

    override fun getSettings(): Flow<List<SettingsSensor>> = _sensorsFlow

    private fun loadAppSettings(): SettingsApp {
        val jsonString = settings.getString(APP_SETTINGS_KEY, "")
        return if (jsonString.isNotEmpty())
            runCatching { json.decodeFromString<SettingsApp>(jsonString) }
                .getOrDefault(SettingsApp(true))
        else SettingsApp(true)
    }

    private fun loadSensors(): List<SettingsSensor> {
        val jsonString = settings.getString(SENSORS_KEY, "")
        return if (jsonString.isNotEmpty())
            runCatching { json.decodeFromString<List<SettingsSensor>>(jsonString) }
                .getOrDefault(emptyList())
        else emptyList()
    }
}