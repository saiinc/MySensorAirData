package com.saionji.mysensor.shared.data.repository

import com.saionji.mysensor.shared.data.model.SettingsApp
import com.saionji.mysensor.shared.data.model.SettingsSensor
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun saveAppSettings(settings: SettingsApp)
    suspend fun saveSettings(sensors: List<SettingsSensor>)
    fun getAppSettings(): Flow<SettingsApp>
    fun getSettings(): Flow<List<SettingsSensor>>
}