/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.russhwolf.settings.Settings
import com.saionji.mysensor.data.AndroidAppContainer
import com.saionji.mysensor.shared.di.SharedContainer
import com.saionji.mysensor.shared.data.repository.DataStoreMigrationRunner
import kotlinx.coroutines.runBlocking

class MySensorApplication : Application() {

    lateinit var container: SharedContainer
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_datastore") // DataStore extension для контекста

    override fun onCreate() {
        super.onCreate()

        runBlocking {
            runMigration()
        }

        container = AndroidAppContainer(applicationContext)
    }

    private suspend fun runMigration() {
        val multiplatformSettings =
            Settings()

        DataStoreMigrationRunner(
            dataStore = applicationContext.dataStore,
            settings = multiplatformSettings
        ).migrateIfNeeded()
    }
}