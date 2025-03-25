/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.saionji.mysensor.domain.GetAllSensorsUseCase
import com.saionji.mysensor.domain.GetAllSensorsUseCaseDev
import com.saionji.mysensor.network.model.SensorService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


// DataStore extension для контекста
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_datastore")

interface AppContainer {
    val mySensorRepository: MySensorRepository
    val settingsRepository: SettingsRepository
    val dataStore: DataStore<Preferences>
    val getAllSensorsUseCase: GetAllSensorsUseCase // Добавляем UseCase
    val getAllSensorsUseCaseDev: GetAllSensorsUseCaseDev // Добавляем UseCase
}

class DefaultAppContainer(context: Context) : AppContainer{
    private val BASE_URL = "https://data.sensor.community/airrohr/v1/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    // DataStore, полученный через контекст
    override val dataStore: DataStore<Preferences> = context.dataStore


    private val retrofitService: SensorService by lazy {
        retrofit.create(SensorService::class.java)
    }


    override val mySensorRepository: MySensorRepository by lazy {
        NetworkMySensorRepository(retrofitService)
    }

    // Репозиторий для работы с настройками
    override val settingsRepository: SettingsRepository = SettingsRepository(context.dataStore)

    override val getAllSensorsUseCase: GetAllSensorsUseCase by lazy {
        GetAllSensorsUseCase(mySensorRepository) // Передаем репозиторий в UseCase
    }

    override val getAllSensorsUseCaseDev: GetAllSensorsUseCaseDev by lazy {
        GetAllSensorsUseCaseDev(mySensorRepository) // Передаем репозиторий в UseCase
    }
}