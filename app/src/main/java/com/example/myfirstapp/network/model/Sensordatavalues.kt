package com.example.myfirstapp.network.model

import com.google.gson.annotations.SerializedName


data class Sensordatavalues (

  @SerializedName("id"         ) var id        : String?    = null,
  @SerializedName("value_type" ) var valueType : String? = null,
  @SerializedName("value"      ) var value     : String? = null

)