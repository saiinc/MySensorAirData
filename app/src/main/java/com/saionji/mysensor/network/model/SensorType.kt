/*
 * Copyright © Anton Sorokin 2024. All rights reserved
 */

package com.saionji.mysensor.network.model

import com.google.gson.annotations.SerializedName


data class SensorType (

  @SerializedName("id"           ) var id           : Int?    = null,
  @SerializedName("manufacturer" ) var manufacturer : String? = null,
  @SerializedName("name"         ) var name         : String? = null

)