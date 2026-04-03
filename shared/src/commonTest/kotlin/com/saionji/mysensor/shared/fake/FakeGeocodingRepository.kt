package com.saionji.mysensor.shared.fake

import com.saionji.mysensor.shared.domain.model.GeocodingRepository

class FakeGeocodingRepository(
    private val addressResult: String = "Test Address, City"
) : GeocodingRepository {

    var callCount = 0
    var lastLat: Double? = null
    var lastLon: Double? = null

    override suspend fun getAddress(lat: Double?, lon: Double?): String {
        callCount++
        lastLat = lat
        lastLon = lon
        return addressResult
    }
}