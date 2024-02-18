package com.example.myfirstapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myfirstapp.ui.MySensorViewModel
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("data_store")

class DataStoreManager(val context: Context) {

    suspend fun saveSettings(settingsData: SettingsData){
        context.dataStore.edit { pref ->
            pref[stringPreferencesKey("sensor_id")] = settingsData.sensorId
            pref[stringSetPreferencesKey("sensor_id_history")] = settingsData.sensorHistory
        }
    }
    fun getSettings() = context.dataStore.data.map { pref ->
        return@map SettingsData(
            pref[stringPreferencesKey("sensor_id")] ?: "",
            pref[stringSetPreferencesKey("sensor_id_history")] ?: setOf()
        )
    }
}