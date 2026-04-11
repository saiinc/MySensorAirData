package com.saionji.mysensor.shared.data.repository

import com.saionji.mysensor.shared.domain.model.GeocodingRepository

class IosGeocodingRepository : GeocodingRepository {
    override suspend fun getAddress(lat: Double?, lon: Double?): String {
        // Заглушка - возвращаем координаты как строку
        if (lat == null || lon == null) return "Unknown location"
        return "${lat.toString().take(10)}, ${lon.toString().take(10)}"
    }
}