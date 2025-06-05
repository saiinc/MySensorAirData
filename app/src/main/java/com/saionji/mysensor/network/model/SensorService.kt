/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.network.model

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SensorService {
    @GET("sensor/{id}/")
    suspend fun getVal(
        @Path("id") sensorId: Int
    ): List<MySensorRawData>

    @GET("filter/box={lat1},{lon1},{lat2},{lon2}")
    suspend fun getSensorsByArea(
        @Path("lat1") lat1: Double,
        @Path("lon1") lon1: Double,
        @Path("lat2") lat2: Double,
        @Path("lon2") lon2: Double
    ): List<MySensorRawData>
}