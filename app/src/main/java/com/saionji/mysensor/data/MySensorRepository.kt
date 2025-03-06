/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

import com.saionji.mysensor.network.model.SensorService

interface MySensorRepository {
    suspend fun getSensor(senorId: String) : List<MySensor>
}

class NetworkMySensorRepository(
    private val sensorService: SensorService
) : MySensorRepository {

    override suspend fun getSensor(
        senorId: String
    ): List<MySensor> =
        try {
            sensorService.getVal(senorId.toInt())[0].sensordatavalues.map { it ->
                try {
                    it.value?.toDouble()
                    MySensor(
                        valueType = it.valueType,
                        value = it.value
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
