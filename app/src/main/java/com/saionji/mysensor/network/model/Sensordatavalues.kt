/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.network.model

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName


data class Sensordatavalues (

  @SerializedName("id"         ) var id        : String? = null,
  @SerializedName("value_type" ) var valueType : String? = null,
  @SerializedName("value"      ) var value     : Double  = 0.0,
                                 var color     : Color   = Color.Transparent
)