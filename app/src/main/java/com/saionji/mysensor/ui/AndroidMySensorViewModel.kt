/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.saionji.mysensor.MySensorApplication
import com.saionji.mysensor.shared.ui.viewmodel.MySensorViewModel as SharedMySensorViewModel
import com.saionji.mysensor.shared.data.repository.SettingsRepository
import com.saionji.mysensor.shared.di.SharedContainer
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesUseCase

/**
 * Android wrapper для Shared ViewModel
 *
 * Обеспечивает:
 * - Android Lifecycle (через наследование ViewModel)
 * - viewModelScope (передается в shared ViewModel)
 * - Совместимость с Android DI
 */
class AndroidMySensorViewModel(
    settingsRepository: SettingsRepository,
    getSensorValuesUseCase: GetSensorValuesUseCase
) : ViewModel() {

    /**
     * Shared ViewModel с бизнес-логикой
     *
     * Публичный доступ для передачи в shared UI компоненты.
     */
    val sharedViewModel = SharedMySensorViewModel(
        settingsRepository = settingsRepository,
        getSensorValuesUseCase = getSensorValuesUseCase,
        scope = viewModelScope  // ✅ Передаем Android viewModelScope
    )


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MySensorApplication)
                val sharedContainer = application.container as SharedContainer

                AndroidMySensorViewModel(
                    settingsRepository = sharedContainer.settingsRepository,
                    getSensorValuesUseCase = sharedContainer.getSensorValuesUseCase
                )
            }
        }
    }
}