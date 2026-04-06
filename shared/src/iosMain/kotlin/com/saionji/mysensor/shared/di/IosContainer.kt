package com.saionji.mysensor.shared.di

import com.russhwolf.settings.Settings
import com.saionji.mysensor.shared.data.repository.NetworkMySensorRepository
import com.saionji.mysensor.shared.data.repository.SettingsRepositoryImpl
import com.saionji.mysensor.shared.domain.repository.MySensorRepository
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesUseCase
import com.saionji.mysensor.shared.network.service.HttpClientFactory
import com.saionji.mysensor.shared.network.service.KtorSensorService

/**
 * DI контейнер для iOS
 */
class IosContainer {

    companion object {
        private const val BASE_URL = "https://data.sensor.community/airrohr/v1/"
    }

    private val settings: Settings = Settings()

    val settingsRepository by lazy {
        SettingsRepositoryImpl(settings)
    }

    private val sensorService by lazy {
        KtorSensorService(
            client = HttpClientFactory.createHttpClient(),
            baseUrl = BASE_URL
        )
    }

    private val mySensorRepository: MySensorRepository by lazy {
        NetworkMySensorRepository(sensorService)
    }

    val getSensorValuesUseCase: GetSensorValuesUseCase by lazy {
        GetSensorValuesUseCase(mySensorRepository)
    }
}