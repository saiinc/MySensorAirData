package com.saionji.mysensor.ui.map.model

data class SelectedMarkerUiState(
    val id: String,
    val lat: Double,
    val lon: Double,
    val valueType: String,
    val value: Double,
    val address: String = "",
    val isAdded: Boolean,
    val isLimitReached: Boolean
)
