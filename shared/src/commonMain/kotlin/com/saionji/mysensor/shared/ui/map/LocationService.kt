package com.saionji.mysensor.shared.ui.map


import com.saionji.mysensor.shared.domain.model.LatLng

/**
 * Service for getting current device location
 * Platform-specific implementation
 */
interface LocationService {

    /**
     * Get current device location
     * @param callback Called with location or null if location cannot be obtained
     */
    suspend fun getCurrentLocation(callback: (LatLng?) -> Unit)
}