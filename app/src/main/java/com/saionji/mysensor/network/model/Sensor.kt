/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.network.model

import com.google.gson.annotations.SerializedName


data class Sensor (

  @SerializedName("sensor_type" ) var sensorType : SensorType? = SensorType(),
  @SerializedName("id"          ) var id         : String?        = null,
  @SerializedName("pin"         ) var pin        : String?     = null

)