package com.saionji.mysensor.shared.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class MySensor(
    var valueType: String,
    var value: String,
    @Transient
    var color: Int = 0
)