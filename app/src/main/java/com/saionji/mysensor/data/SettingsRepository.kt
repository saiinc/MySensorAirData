/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val MY_OBJECTS_KEY = stringPreferencesKey("my_objects_key")
    private val APP_SETTINGS_KEY = stringPreferencesKey("app_settings")

    suspend fun saveAppSettings(settings: SettingsApp) {
        val jsonString = json.encodeToString(settings)
        dataStore.edit { preferences ->
            preferences[APP_SETTINGS_KEY] = jsonString
        }
    }

    suspend fun saveSettings(sensors: List<SettingsSensor>) {
        val jsonString = json.encodeToString(sensors)
        dataStore.edit { preferences ->
            preferences[MY_OBJECTS_KEY] = jsonString
        }
    }

    fun getAppSettings(): Flow<SettingsApp> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[APP_SETTINGS_KEY] ?: ""
            if (jsonString.isNotEmpty()) {
                try {
                    json.decodeFromString<SettingsApp>(jsonString)
                } catch (e: Exception) {
                    // Если старый формат — сбрасываем
                    SettingsApp(true)
                }
            } else {
                SettingsApp(true)
            }
        }
    }

    fun getSettings(): Flow<List<SettingsSensor>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[MY_OBJECTS_KEY] ?: ""
            if (jsonString.isNotEmpty()) {
                try {
                    json.decodeFromString<List<SettingsSensor>>(jsonString)
                } catch (e: Exception) {
                    // Старый Gson-формат или старый Color
                    emptyList()
                }
            } else {
                emptyList()
            }
        }
    }
}
