package com.saionji.mysensor.shared.domain.model

data class MapMarker(
    val id: String,
    val lat: Double,
    val lon: Double,
    val valueType: String,
    val value: String,
    val colorInt: Int
)