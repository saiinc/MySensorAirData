package com.saionji.mysensor.shared.ui.map

sealed interface MapUiState {
    object Idle : MapUiState
    object Loading : MapUiState
    data class Success(val markers: List<com.saionji.mysensor.shared.domain.model.MapMarker>) : MapUiState
    data class Error(val message: String) : MapUiState
}