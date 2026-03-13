/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

import android.content.Context
import com.russhwolf.settings.Settings
import com.saionji.mysensor.shared.di.SharedContainer
import com.saionji.mysensor.shared.di.SharedContainerImpl
import com.saionji.mysensor.shared.di.AndroidPlatformDependencies

/**
 * Интерфейс контейнера зависимостей для совместимости
 *
 * Наследует SharedContainer для сохранения обратной совместимости
 */
interface AppContainer : SharedContainer

/**
 * Android-реализация контейнера зависимостей
 *
 * Создает платформенные зависимости и делегирует остальное в SharedContainerImpl
 */
class AndroidAppContainer(
    private val context: Context
) : AppContainer {

    /**
     * Платформенные зависимости (GeocodingRepository)
     */
    private val platformDependencies = AndroidPlatformDependencies()

    /**
     * Общий контейнер зависимостей
     *
     * Содержит всю бизнес-логику и репозитории
     */
    private val sharedContainer by lazy {
        val geocodingRepository = platformDependencies.createGeocodingRepository(context)
        val settings = Settings()
        SharedContainerImpl(geocodingRepository, settings)
    }

    // ==================== ДЕЛЕГИРОВАНИЕ В SHARED CONTAINER ====================

    /**
     * Репозиторий для работы с данными сенсоров
     */
    override val mySensorRepository get() = sharedContainer.mySensorRepository

    /**
     * Репозиторий для хранения настроек
     */
    override val settingsRepository get() = sharedContainer.settingsRepository

    /**
     * UseCase для получения данных сенсора по ID
     */
    override val getSensorValuesUseCase get() = sharedContainer.getSensorValuesUseCase

    /**
     * UseCase для получения сенсоров по области
     */
    override val getSensorValuesByAreaUseCase get() = sharedContainer.getSensorValuesByAreaUseCase

    /**
     * UseCase для получения адреса по координатам
     */
    override val getAddressFromCoordinatesUseCase get() = sharedContainer.getAddressFromCoordinatesUseCase
}