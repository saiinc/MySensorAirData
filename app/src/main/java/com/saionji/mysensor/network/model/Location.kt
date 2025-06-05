/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */


package com.saionji.mysensor.network.model

import com.google.gson.annotations.SerializedName


data class Location (

  @SerializedName("longitude"      ) var longitude     : Double = 0.0,
  @SerializedName("exact_location" ) var exactLocation : Int    = 0,
  @SerializedName("id"             ) var id            : Int    = 0,
  @SerializedName("latitude"       ) var latitude      : Double = 0.0,
  @SerializedName("indoor"         ) var indoor        : Int    = 0,
  @SerializedName("altitude"       ) var altitude      : String = "",
  @SerializedName("country"        ) var country       : String = ""

)