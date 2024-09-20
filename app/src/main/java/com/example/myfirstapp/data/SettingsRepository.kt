package com.example.myfirstapp.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension для получения DataStore из контекста
val Context.dataStore by preferencesDataStore("my_datastore")

class SettingsRepository(private val context: Context) {

    private val gson = Gson()
    private val MY_OBJECTS_KEY = stringPreferencesKey("my_objects_key")

    // Функция для сохранения списка объектов
    suspend fun saveSettings(myObjects: List<SettingsSensor>) {
        val jsonString = gson.toJson(myObjects) // Преобразуем список объектов в JSON
        context.dataStore.edit { preferences ->
            preferences[MY_OBJECTS_KEY] = jsonString
        }
    }

    // Функция для загрузки списка объектов
    fun getSettings(): Flow<List<SettingsSensor>> {
        return context.dataStore.data
            .map { preferences ->
                val jsonString = preferences[MY_OBJECTS_KEY] ?: ""
                if (jsonString.isNotEmpty()) {
                    val type = object : TypeToken<List<SettingsSensor>>() {}.type
                    gson.fromJson<List<SettingsSensor>>(jsonString, type) // Преобразуем JSON обратно в список объектов
                } else {
                    listOf(SettingsSensor("", ""))
                    //emptyList() // Возвращаем пустой список, если данных нет
                }
            }
    }
}
