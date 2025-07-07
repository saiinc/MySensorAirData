/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private val gson = Gson()
    private val MY_OBJECTS_KEY = stringPreferencesKey("my_objects_key")
    private val APP_SETTINGS = stringPreferencesKey("app_settings")

    suspend fun saveAppSettings(myObjects: SettingsApp) {
        val jsonString = gson.toJson(myObjects) // Преобразуем список объектов в JSON
        dataStore.edit { preferences ->
            preferences[APP_SETTINGS] = jsonString
        }
    }

    // Функция для сохранения списка объектов
    suspend fun saveSettings(myObjects: List<SettingsSensor>) {
        val jsonString = gson.toJson(myObjects)
        dataStore.edit { preferences ->
            preferences[MY_OBJECTS_KEY] = jsonString
        }
    }

    fun getAppSettings(): Flow<SettingsApp> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[APP_SETTINGS] ?: ""
            if (jsonString.isNotEmpty()) {
                val type = object : TypeToken<SettingsApp>() {}.type
                gson.fromJson(jsonString, type)
            }
            else {
                SettingsApp(true)
            }
        }
    }

    // Функция для загрузки списка объектов
    fun getSettings(): Flow<List<SettingsSensor>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[MY_OBJECTS_KEY] ?: ""
            if (jsonString.isNotEmpty()) {
                val type = object : TypeToken<List<SettingsSensor>>() {}.type
                gson.fromJson(jsonString, type) // Преобразуем JSON обратно в список объектов
            }
            else {
                emptyList() // Возвращаем пустой список, если данных нет
            }
        }
    }
}
