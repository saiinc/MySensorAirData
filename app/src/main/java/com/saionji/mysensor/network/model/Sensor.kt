/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Sensor(
    @SerialName("sensor_type")
    val sensorType: SensorType? = null,

    @SerialName("id")
    val id: Int? = null,

    @SerialName("pin")
    val pin: String? = null
)