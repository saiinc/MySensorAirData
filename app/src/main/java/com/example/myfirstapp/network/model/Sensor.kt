package com.example.myfirstapp

import com.google.gson.annotations.SerializedName


data class Sensor (

  @SerializedName("sensor_type" ) var sensorType : SensorType? = SensorType(),
  @SerializedName("id"          ) var id         : Int?        = null,
  @SerializedName("pin"         ) var pin        : String?     = null

)