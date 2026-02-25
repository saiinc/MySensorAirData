package com.saionji.mysensor.shared.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sensordatavalues(
    @SerialName("id")
    val id: Long? = null,

    @SerialName("value_type")
    var valueType: String? = null,

    @SerialName("value")
    var value: Double? = null
)