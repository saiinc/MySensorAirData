/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.network.model

interface SensorService {
    suspend fun getVal(sensorId: Int): List<MySensorRawData>
    suspend fun getSensorsByArea(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MySensorRawData>
}