/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor

import android.app.Application
import com.saionji.mysensor.data.AndroidAppContainer
import com.saionji.mysensor.data.AppContainer

class MySensorApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AndroidAppContainer(applicationContext)
    }
}