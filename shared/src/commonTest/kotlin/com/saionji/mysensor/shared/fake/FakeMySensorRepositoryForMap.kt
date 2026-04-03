package com.saionji.mysensor.shared.fake

import com.saionji.mysensor.shared.data.MySensor
import com.saionji.mysensor.shared.domain.model.MapSensor
import com.saionji.mysensor.shared.domain.repository.MySensorRepository

class FakeMySensorRepositoryForMap(
    private val shouldFail: Boolean = false,
    private val markers: List<MapSensor> = emptyList()
) : MySensorRepository {

    data class Bounds(
        val lat1: Double,
        val lon1: Double,
        val lat2: Double,
        val lon2: Double
    )

    var callCount = 0
    var lastBounds: Bounds? = null

    override suspend fun getSensor(sensorId: String): List<MySensor> {
        return emptyList()
    }

    override suspend fun getSensorDataByArea(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MapSensor> {
        if (shouldFail) throw Exception("Network error")

        callCount++
        lastBounds = Bounds(lat1, lon1, lat2, lon2)
        return markers
    }
}