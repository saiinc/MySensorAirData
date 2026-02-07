package com.saionji.mysensor.data

data class MapSensor(
    val id: String,
    val lat: Double,
    val lon: Double,
    val measurements: List<MapMeasurement> = emptyList()
)
