/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor

import android.app.Application
import com.saionji.mysensor.data.AppContainer
import com.saionji.mysensor.data.DefaultAppContainer

class MySensorApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}