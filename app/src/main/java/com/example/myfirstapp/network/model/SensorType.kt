package com.example.myfirstapp

import com.google.gson.annotations.SerializedName


data class SensorType (

  @SerializedName("id"           ) var id           : Int?    = null,
  @SerializedName("manufacturer" ) var manufacturer : String? = null,
  @SerializedName("name"         ) var name         : String? = null

)