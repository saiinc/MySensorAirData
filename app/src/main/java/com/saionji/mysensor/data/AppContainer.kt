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
 * Android-реализация контейнера зависимостей
 *
 * Делегирует все зависимости в SharedContainerImpl
 */
class AndroidAppContainer(
    context: Context
) : SharedContainer by SharedContainerImpl(
    geocodingRepository = AndroidPlatformDependencies().createGeocodingRepository(context),
    settings = Settings()
)