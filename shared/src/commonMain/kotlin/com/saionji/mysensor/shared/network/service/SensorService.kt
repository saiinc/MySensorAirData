package com.saionji.mysensor.shared.network.service

import com.saionji.mysensor.shared.network.model.MySensorRawData

interface SensorService {
    suspend fun getVal(sensorId: Int): List<MySensorRawData>
    suspend fun getSensorsByArea(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MySensorRawData>
}