package com.saionji.mysensor.shared.domain.repository

import com.saionji.mysensor.shared.data.MySensor
import com.saionji.mysensor.shared.domain.model.MapSensor

interface MySensorRepository {
    suspend fun getSensor(senorId: String) : List<MySensor>
    suspend fun getSensorDataByArea(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MapSensor>
}