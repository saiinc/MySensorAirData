package com.saionji.mysensor.shared.domain.model

data class MapSensor(
    val id: String,
    val lat: Double,
    val lon: Double,
    val measurements: List<MapMeasurement> = emptyList()
)