/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor

import android.app.Application
import com.russhwolf.settings.Settings
import com.saionji.mysensor.data.AndroidAppContainer
import com.saionji.mysensor.data.AppContainer
import com.saionji.mysensor.data.dataStore
import com.saionji.mysensor.shared.data.repository.DataStoreMigrationRunner
import kotlinx.coroutines.runBlocking

class MySensorApplication : Application() {

    lateinit var container: AppContainer

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