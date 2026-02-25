package com.saionji.mysensor.shared.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MySensorRawData(
    @SerialName("timestamp")
    val timestamp: String? = null,

    @SerialName("sampling_rate")
    val samplingRate: String? = null,

    @SerialName("id")
    val id: Long? = null,

    @SerialName("sensordatavalues")
    val sensordatavalues: List<Sensordatavalues> = emptyList(),

    @SerialName("sensor")
    val sensor: Sensor? = null,

    @SerialName("location")
    val location: Location? = null
)