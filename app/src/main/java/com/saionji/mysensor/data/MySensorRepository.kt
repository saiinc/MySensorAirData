/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

import android.util.Log
import com.saionji.mysensor.network.model.MySensorRawData
import com.saionji.mysensor.network.model.SensorService

interface MySensorRepository {
    suspend fun getSensor(senorId: String) : List<MySensor>
    suspend fun getSensorDataByArea(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MySensorRawData>
}

class NetworkMySensorRepository(
    private val sensorService: SensorService
) : MySensorRepository {

    override suspend fun getSensorDataByArea(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MySensorRawData> {
        return try {
            val result = sensorService.getSensorsByArea(lat1, lon1, lat2, lon2)
            Log.d("MapDebug", "Received ${result.size} sensors")

            // Удаляем дубликаты по id (или другому уникальному признаку)
            val distinct = result.distinctBy { it.sensor?.id }
            val outdoor = distinct.filter { it.location.indoor == 0 }

            Log.d("MapDebug", "Filtered to ${distinct.size} unique sensors")
            outdoor
        } catch (e: Exception) {
            Log.e("MapDebug", "Error fetching sensors: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getSensor(
        senorId: String
    ): List<MySensor> =
        try {
            sensorService.getVal(senorId.toInt())[0].sensordatavalues.map { it ->
                try {
                    it.value
                    MySensor(
                        valueType = it.valueType,
                        value = it.value.toString()
                    )
                } catch (e: NumberFormatException) {
                    MySensor(
                        valueType = it.valueType,
                        value = "0"
                    )
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            listOf(MySensor(
                valueType = "Sensor \"$senorId\" not found",
                value = ""
            ))
        } catch (e: NumberFormatException) {
            listOf(MySensor(
                valueType = "Sensor \"$senorId\" not found",
                value = ""
            ))
        }
    }
