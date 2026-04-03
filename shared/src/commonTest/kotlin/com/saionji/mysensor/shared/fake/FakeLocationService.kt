package com.saionji.mysensor.shared.fake

import com.saionji.mysensor.shared.domain.model.LatLng
import com.saionji.mysensor.shared.ui.map.LocationService

class FakeLocationService(
    private val location: LatLng? = LatLng(55.7558, 37.6173)
) : LocationService {

    var callCount = 0

    override suspend fun getCurrentLocation(callback: (LatLng?) -> Unit) {
        callCount++
        callback(location)
    }
}