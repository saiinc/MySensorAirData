package com.saionji.mysensor.shared.di

/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

import android.content.Context
import android.location.Geocoder
import com.saionji.mysensor.shared.domain.model.GeocodingRepository

/**
 * Интерфейс для платформенных зависимостей
 *
 * Этот интерфейс позволяет создавать зависимости, специфичные для конкретной платформы.
 * Например: Geocoder на Android использует Android API, на iOS - iOS API.
 */
interface PlatformDependencies {
    /**
     * Создать репозиторий геокодинга для платформы
     *
     * @param context Android контекст (только для Android)
     * @return Реализация GeocodingRepository для платформы
     */
    fun createGeocodingRepository(context: Context): GeocodingRepository
}

/**
 * Android-реализация платформенных зависимостей
 */
class AndroidPlatformDependencies : PlatformDependencies {
    override fun createGeocodingRepository(context: Context): GeocodingRepository {
        val geocoder = Geocoder(context)
        return com.saionji.mysensor.shared.data.repository.AndroidGeocodingRepository(geocoder)
    }
}