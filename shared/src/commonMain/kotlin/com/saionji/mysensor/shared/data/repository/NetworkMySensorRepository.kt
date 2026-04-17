package com.saionji.mysensor.shared.data.repository

import com.saionji.mysensor.shared.data.MySensor
import com.saionji.mysensor.shared.domain.model.MapMeasurement
import com.saionji.mysensor.shared.domain.model.MapSensor
import com.saionji.mysensor.shared.domain.repository.MySensorRepository
import com.saionji.mysensor.shared.network.service.SensorService

class NetworkMySensorRepository(
    private val sensorService: SensorService
) : MySensorRepository {

    override suspend fun getSensorDataByArea(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MapSensor> {
        return try {
            val result = sensorService.getSensorsByArea(lat1, lon1, lat2, lon2)
            val outdoor = result
                .distinctBy { it.sensor?.id }
                .filter { it.location?.indoor == 0 }

            outdoor.mapNotNull { dto ->
                val id = dto.sensor?.id ?: return@mapNotNull null
                val lat = dto.location?.latitude
                val lon = dto.location?.longitude

                val measurements = dto.sensordatavalues.map { valueDto ->
                    MapMeasurement(
                        value = valueDto.value ?: return@mapNotNull null,
                        valueType = valueDto.valueType ?: return@mapNotNull null
                    )
                }

                MapSensor(
                    id = id.toString(),
                    lat = lat ?: return@mapNotNull null,
                    lon = lon ?: return@mapNotNull null,
                    measurements = measurements
                )
            }.also {
                println("MAP_RESPONSE mapped=${it.size}")
            }
        } catch (e: Exception) {
            println("MAP_RESPONSE error=${e.message}")
            emptyList()
        }
    }

    override suspend fun getSensor(
        senorId: String
    ): List<MySensor> =
        try {
            sensorService.getVal(senorId.toInt())[0].sensordatavalues.mapNotNull {
                try {
                    it.value
                    MySensor(
                        valueType = it.valueType ?: return@mapNotNull null,
                        value = it.value.toString()
                    )
                } catch (_: NumberFormatException) {
                    MySensor(
                        valueType = it.valueType ?: return@mapNotNull null,
                        value = "0"
                    )
                }
            }
        } catch (_: IndexOutOfBoundsException) {
            listOf(
                MySensor(
                    valueType = "Sensor \"$senorId\" not found",
                    value = ""
                )
            )
        } catch (_: NumberFormatException) {
            listOf(
                MySensor(
                    valueType = "Sensor \"$senorId\" not found",
                    value = ""
                )
            )
        }
}
