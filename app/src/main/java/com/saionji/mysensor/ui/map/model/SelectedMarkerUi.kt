package com.saionji.mysensor.ui.map.model

data class SelectedMarkerUi(
    val id: String,
    val lat: Double,
    val lon: Double,
    val valueType: String,
    val value: String
)
