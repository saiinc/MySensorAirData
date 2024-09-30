/*
 * Copyright © Anton Sorokin 2024. All rights reserved
 */

package com.saionji.mysensor.data


data class MyDevice(
    val id                 : String?,
    val description        : String?,
    val deviceSensors      : List<MySensor>
)
