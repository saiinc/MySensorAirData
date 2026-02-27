package com.saionji.mysensor.shared.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class DataStoreMigrationRunner(
    private val dataStore: DataStore<Preferences>,
    private val settings: Settings
) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val APP_SETTINGS_KEY = stringPreferencesKey("app_settings")
    private val SENSORS_KEY = stringPreferencesKey("my_objects_key")

    suspend fun migrateIfNeeded() {
        val alreadyMigrated = settings.getBoolean("migration_done", false)
        if (alreadyMigrated) return

        val preferences = dataStore.data.first()

        val appSettingsJson = preferences[APP_SETTINGS_KEY]
        val sensorsJson = preferences[SENSORS_KEY]

        if (!appSettingsJson.isNullOrEmpty()) {
            settings.putString("app_settings", appSettingsJson)
        }

        if (!sensorsJson.isNullOrEmpty()) {
            settings.putString("my_objects_key", sensorsJson)
        }

        settings.putBoolean("migration_done", true)

        // можно очистить DataStore (необязательно)
        dataStore.edit { it.clear() }
    }
}