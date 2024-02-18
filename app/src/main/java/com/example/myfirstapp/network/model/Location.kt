package com.example.myfirstapp

import com.google.gson.annotations.SerializedName


data class Location (

  @SerializedName("longitude"      ) var longitude     : String? = null,
  @SerializedName("exact_location" ) var exactLocation : Int?    = null,
  @SerializedName("id"             ) var id            : Int?    = null,
  @SerializedName("latitude"       ) var latitude      : String? = null,
  @SerializedName("indoor"         ) var indoor        : Int?    = null,
  @SerializedName("altitude"       ) var altitude      : String? = null,
  @SerializedName("country"        ) var country       : String? = null

)