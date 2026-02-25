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
import com.saionji.mysensor.shared.data.repository.NetworkMySensorRepository
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesUseCase
import com.saionji.mysensor.shared.domain.model.GeocodingRepository
import com.saionji.mysensor.shared.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.shared.network.service.KtorSensorService
import com.saionji.mysensor.shared.domain.repository.MySensorRepository
import com.saionji.mysensor.shared.network.service.HttpClientFactory
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
    val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase
    val getSensorValuesUseCase: GetSensorValuesUseCase // Добавляем UseCase
    val getAddressFromCoordinatesUseCase: GetAddressFromCoordinatesUseCase
}

class AndroidAppContainer(
    private val context: Context
) : AppContainer {

    private companion object {
        const val BASE_URL = "https://data.sensor.community/airrohr/v1/"
    }

    private val sensorService by lazy {
        KtorSensorService(HttpClientFactory.createHttpClient(), BASE_URL)
    }

    private val repository by lazy {
        NetworkMySensorRepository(sensorService)
    }

    override val mySensorRepository: MySensorRepository
        get() = repository

    override val settingsRepository by lazy {
        AndroidSettingsRepository(context.dataStore)
    }

    private val geocodingRepository by lazy {
        createGeocodingRepository(context)
    }

    override val getSensorValuesUseCase by lazy {
        GetSensorValuesUseCase(repository)
    }

    override val getSensorValuesByAreaUseCase by lazy {
        GetSensorValuesByAreaUseCase(repository)
    }

    override val getAddressFromCoordinatesUseCase by lazy {
        GetAddressFromCoordinatesUseCase(geocodingRepository)
    }

    private fun createGeocodingRepository(context: Context): GeocodingRepository {
        val geocoder = Geocoder(
            context.applicationContext,
            Locale.current.platformLocale
        )
        return AndroidGeocodingRepository(geocoder)
    }
}