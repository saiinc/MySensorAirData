/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.data

data class SettingsSensor(
    var id: String,
    var description: String,
    var deviceSensors      : List<MySensor>
)
