package com.example.myfirstapp.data

import android.hardware.SensorPrivacyManager.Sensors

data class MyDevice(
    val id                 : String?,
    val description        : String?,
    val deviceSensors      : List<MySensor>
)
