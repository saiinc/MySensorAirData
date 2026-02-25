package com.saionji.mysensor.shared.data.model

import com.saionji.mysensor.shared.data.MySensor

data class DashboardSensor(
    val id: String,
    val description: String,
    val deviceSensors: List<MySensor>,
    val isLoading: Boolean = false
)