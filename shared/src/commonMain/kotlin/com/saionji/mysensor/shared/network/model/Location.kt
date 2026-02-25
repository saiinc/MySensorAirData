package com.saionji.mysensor.shared.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    @SerialName("longitude")
    val longitude: Double? = null,

    @SerialName("exact_location")
    val exactLocation: Int? = null,

    @SerialName("id")
    val id: Int? = null,

    @SerialName("latitude")
    val latitude: Double? = null,

    @SerialName("indoor")
    val indoor: Int? = null,

    @SerialName("altitude")
    val altitude: String? = null,

    @SerialName("country")
    val country: String? = null
)