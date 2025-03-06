/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.network.model

import retrofit2.http.GET
import retrofit2.http.Path

interface SensorService {
    @GET("sensor/{id}/")
    suspend fun getVal(
        @Path("id") sensorId: Int
    ): List<MySensorRawData>

}