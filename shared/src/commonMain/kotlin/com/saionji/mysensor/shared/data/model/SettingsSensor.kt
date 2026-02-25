package com.saionji.mysensor.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SettingsSensor(
    var id: String,
    var description: String,
)