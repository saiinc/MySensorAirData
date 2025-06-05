/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.saionji.mysensor.domain.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.domain.GetSensorValuesUseCase
import com.saionji.mysensor.network.model.SensorService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// DataStore extension для контекста
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_datastore")

interface AppContainer {
    val mySensorRepository: MySensorRepository
    val settingsRepository: SettingsRepository
    val dataStore: DataStore<Preferences>
    val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase
    val getSensorValuesUseCase: GetSensorValuesUseCase // Добавляем UseCase
}

class DefaultAppContainer(context: Context) : AppContainer{
    private val BASE_URL = "https://data.sensor.community/airrohr/v1/"

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Double::class.java, SafeDoubleDeserializer())
        .create()

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create(gson))
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService: SensorService by lazy {
        retrofit.create(SensorService::class.java)
    }

    // DataStore, полученный через контекст
    override val dataStore: DataStore<Preferences> = context.dataStore

    override val mySensorRepository: MySensorRepository by lazy {
        NetworkMySensorRepository(retrofitService)
    }

    // Репозиторий для работы с настройками
    override val settingsRepository: SettingsRepository = SettingsRepository(context.dataStore)

    override val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase by lazy {
        GetSensorValuesByAreaUseCase(mySensorRepository)
    }

    override val getSensorValuesUseCase: GetSensorValuesUseCase by lazy {
        GetSensorValuesUseCase(mySensorRepository) // Передаем репозиторий в UseCase
    }
}