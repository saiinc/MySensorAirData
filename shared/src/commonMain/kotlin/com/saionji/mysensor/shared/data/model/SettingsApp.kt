package com.saionji.mysensor.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SettingsApp(
    val shareId: Boolean
)