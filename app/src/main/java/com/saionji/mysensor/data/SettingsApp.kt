/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

import kotlinx.serialization.Serializable

@Serializable
data class SettingsApp(
    val shareId: Boolean
)
