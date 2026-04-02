package com.saionji.mysensor.shared.fake

import com.saionji.mysensor.shared.data.MySensor
import com.saionji.mysensor.shared.domain.model.MapSensor
import com.saionji.mysensor.shared.domain.repository.MySensorRepository
import io.ktor.utils.io.errors.IOException

class FakeMySensorRepository(
    private val shouldFail: Boolean = false,
    private val customResult: List<MySensor>? = null
) : MySensorRepository {

    // Для проверки вызовов в тестах
    var callCount = 0
        private set
    var lastSensorId: String? = null
        private set

    override suspend fun getSensor(sensorId: String): List<MySensor> {
        callCount++
        lastSensorId = sensorId

        if (shouldFail) {
            throw IOException("Network error")
        }

        return customResult ?: listOf(
            MySensor(
                valueType = "PM2.5",
                value = "15.2",
                color = 0xFF00FF00.toInt()
            ),
            MySensor(
                valueType = "PM10",
                value = "25.0",
                color = 0xFFFFFF00.toInt()
            ),
            MySensor(
                valueType = "temperature",
                value = "22.5",
                color = 0xFF0000FF.toInt()
            )
        )
    }

    override suspend fun getSensorDataByArea(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MapSensor> {
        return emptyList() // Не используется в тестах ViewModel
    }
}