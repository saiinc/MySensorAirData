package com.example.myfirstapp.data

import com.example.myfirstapp.network.model.SensorService

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
                MySensor(
                    valueType = it.valueType,
                    value = it.value
                )
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
