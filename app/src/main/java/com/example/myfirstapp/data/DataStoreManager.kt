package com.example.myfirstapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("data_store")

class DataStoreManager(private val context: Context) {

    suspend fun saveSettings(settingsData: SettingsData){
        context.dataStore.edit { pref ->
            pref[stringSetPreferencesKey("sensor_id_list")] = settingsData.sensorIdList
        }
    }
    fun getSettings() = context.dataStore.data.map { pref ->
        return@map SettingsData(
            pref[stringSetPreferencesKey("sensor_id_list")] ?: setOf("")
        )
    }
}