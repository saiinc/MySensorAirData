package com.saionji.mysensor.shared.data.repository

import com.saionji.mysensor.shared.network.model.MySensorRawData
import com.saionji.mysensor.shared.network.service.SensorService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class NetworkMySensorRepositoryTest {

    @Test
    fun `getSensorDataByArea should propagate service failure`() = runTest {
        val service = FailingSensorService()
        val repository = NetworkMySensorRepository(service)

        assertFailsWith<Exception> {
            repository.getSensorDataByArea(
                lat1 = 56.0,
                lon1 = 38.0,
                lat2 = 55.0,
                lon2 = 37.0
            )
        }
    }

    private class FailingSensorService : SensorService {
        override suspend fun getVal(sensorId: Int): List<MySensorRawData> {
            error("Not used in this test")
        }

        override suspend fun getSensorsByArea(
            lat1: Double,
            lon1: Double,
            lat2: Double,
            lon2: Double
        ): List<MySensorRawData> {
            throw IllegalStateException("Network error")
        }
    }
}