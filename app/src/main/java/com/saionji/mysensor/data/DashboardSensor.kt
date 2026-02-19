package com.saionji.mysensor.data

data class DashboardSensor(
    val id: String,
    val description: String,
    val deviceSensors: List<MySensor>,
    val isLoading: Boolean = false
)
