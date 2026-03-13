package com.saionji.mysensor.shared.di

import com.saionji.mysensor.shared.data.repository.SettingsRepository
import com.saionji.mysensor.shared.domain.model.GetAddressFromCoordinatesUseCase
import com.saionji.mysensor.shared.domain.repository.MySensorRepository
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesByAreaUseCase
import com.saionji.mysensor.shared.domain.usecase.GetSensorValuesUseCase

/**
 * Общий интерфейс контейнера зависимостей для всех платформ
 *
 * Этот интерфейс определяет все зависимости, которые нужны приложению:
 * - Репозитории (источники данных)
 * - UseCase (бизнес-логика)
 * - Другие компоненты
 */
interface SharedContainer {
    /**
     * Репозиторий для работы с данными сенсоров
     */
    val mySensorRepository: MySensorRepository

    /**
     * Репозиторий для хранения настроек
     */
    val settingsRepository: SettingsRepository

    /**
     * UseCase для получения данных сенсора по ID
     */
    val getSensorValuesUseCase: GetSensorValuesUseCase

    /**
     * UseCase для получения сенсоров по области
     */
    val getSensorValuesByAreaUseCase: GetSensorValuesByAreaUseCase

    /**
     * UseCase для получения адреса по координатам
     */
    val getAddressFromCoordinatesUseCase: GetAddressFromCoordinatesUseCase
}