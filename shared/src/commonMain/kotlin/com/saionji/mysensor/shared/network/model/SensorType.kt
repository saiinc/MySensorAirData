package com.saionji.mysensor.shared.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorType(
    @SerialName("id")
    val id: Int? = null,

    @SerialName("manufacturer")
    val manufacturer: String? = null,

    @SerialName("name")
    val name: String? = null
)