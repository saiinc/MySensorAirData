/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.shared.di

import com.saionji.mysensor.shared.domain.repository.MySensorRepository
import com.saionji.mysensor.shared.data.repository.NetworkMySensorRepository
import com.saionji.mysensor.shared.data.repository.SettingsRepository
import com.saionji.mysensor.shared.data.repository.SettingsRepositoryImpl
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesUseCase
import com.saionji.mysensor.shared.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.shared.domain.model.GeocodingRepository
import com.saionji.mysensor.shared.network.service.KtorSensorService
import com.saionji.mysensor.shared.network.service.HttpClientFactory
import com.russhwolf.settings.Settings

/**
 * Общая реализация контейнера зависимостей
 *
 * Эта реализация создает все зависимости, которые являются кроссплатформенными.
 * Платформенные зависимости (например, GeocodingRepository) передаются извне.
 */
class SharedContainerImpl(
    private val geocodingRepository: GeocodingRepository,
    private val settings: Settings
) : SharedContainer {

    companion object {
        private const val BASE_URL = "https://data.sensor.community/airrohr/v1/"
    }

    // ==================== СЕТЕВОЙ СЛОЙ ====================

    /**
     * HTTP клиент для работы с API
     */
    private val sensorService by lazy {
        KtorSensorService(HttpClientFactory.createHttpClient(), BASE_URL)
    }

    // ==================== РЕПОЗИТОРИИ ====================

    /**
     * Репозиторий для работы с данными сенсоров
     *
     * Использует сетевой сервис для получения данных
     */
    private val repository by lazy {
        NetworkMySensorRepository(sensorService)
    }

    /**
     * Репозиторий для хранения настроек
     *
     * Использует multiplatform-settings для кроссплатформенного хранения
     */
    private val multiplatformSettings by lazy {
        settings
    }

    private val settingsRepositoryImpl by lazy {
        SettingsRepositoryImpl(multiplatformSettings)
    }

    // ==================== USE CASE ====================

    /**
     * UseCase для получения данных сенсора по ID
     */
    override val getSensorValuesUseCase: GetSensorValuesUseCase by lazy {
        GetSensorValuesUseCase(repository)
    }

    /**
     * UseCase для получения сенсоров по области
     */
    override val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase by lazy {
        GetSensorValuesByAreaUseCase(repository)
    }

    /**
     * UseCase для получения адреса по координатам
     *
     * Использует платформенный GeocodingRepository
     */
    override val getAddressFromCoordinatesUseCase: GetAddressFromCoordinatesUseCase by lazy {
        GetAddressFromCoordinatesUseCase(geocodingRepository)
    }

    // ==================== ПУБЛИЧНЫЕ СВОЙСТВА ====================

    override val mySensorRepository: MySensorRepository
        get() = repository

    override val settingsRepository: SettingsRepository
        get() = settingsRepositoryImpl
    }