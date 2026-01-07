package com.saionji.mysensor.ui.map.model

import com.saionji.mysensor.network.model.MySensorRawData

data class MapMarkerUiModel(
    val id: String,
    val lat: Double,
    val lon: Double,
    val valueType: String,
    val value: Double,
    val colorInt: Int
)