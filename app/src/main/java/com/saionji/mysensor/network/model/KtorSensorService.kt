package com.saionji.mysensor.network.model

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorSensorService(
    private val client: HttpClient,
    private val baseUrl: String
) : SensorService {

    override suspend fun getVal(sensorId: Int): List<MySensorRawData> {
        return client.get("$baseUrl/sensor/$sensorId/")
            .body()
    }

    override suspend fun getSensorsByArea(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): List<MySensorRawData> {
        return client.get(
            "$baseUrl/filter/box=$lat1,$lon1,$lat2,$lon2"
        ).body()
    }
}