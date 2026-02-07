/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

import android.content.Context
import android.location.Geocoder
import androidx.compose.ui.text.intl.Locale
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.saionji.mysensor.domain.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.domain.GetSensorValuesUseCase
import com.saionji.mysensor.domain.model.GeocodingRepository
import com.saionji.mysensor.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.network.model.KtorSensorService
import com.saionji.mysensor.network.model.SensorService
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


// DataStore extension для контекста
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_datastore")

interface AppContainer {
    val mySensorRepository: MySensorRepository
    val settingsRepository: SettingsRepository
    val dataStore: DataStore<Preferences>
    val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase
    val getSensorValuesUseCase: GetSensorValuesUseCase // Добавляем UseCase

    val getAddressFromCoordinatesUseCase: GetAddressFromCoordinatesUseCase
}

class DefaultAppContainer(context: Context) : AppContainer{
    private val BASE_URL = "https://data.sensor.community/airrohr/v1/"

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    private val sensorService: SensorService by lazy {
        KtorSensorService(client, BASE_URL)
    }

    override val mySensorRepository: MySensorRepository by lazy {
        NetworkMySensorRepository(sensorService)
    }

    // DataStore, полученный через контекст
    override val dataStore: DataStore<Preferences> = context.dataStore

    // Репозиторий для работы с настройками
    override val settingsRepository by lazy {
        SettingsRepository(context.dataStore)
    }

    override val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase by lazy {
        GetSensorValuesByAreaUseCase(mySensorRepository)
    }

    override val getSensorValuesUseCase: GetSensorValuesUseCase by lazy {
        GetSensorValuesUseCase(mySensorRepository) // Передаем репозиторий в UseCase
    }

    // --- Android stuff ---
    private val geocoder = Geocoder(
        context.applicationContext,
        Locale.current.platformLocale
    )

    // --- Data ---
    private val geocodingRepository: GeocodingRepository =
        AndroidGeocodingRepository(geocoder)

    // --- Domain ---
    override val getAddressFromCoordinatesUseCase: GetAddressFromCoordinatesUseCase by lazy {
        GetAddressFromCoordinatesUseCase(geocodingRepository)
    }
}