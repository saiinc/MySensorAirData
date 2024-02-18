package com.example.myfirstapp

import android.app.Application
import com.example.myfirstapp.data.AppContainer
import com.example.myfirstapp.data.DefaultAppContainer

class MySensorApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}