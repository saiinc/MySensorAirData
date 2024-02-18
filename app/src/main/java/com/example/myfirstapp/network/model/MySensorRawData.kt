package com.example.myfirstapp.network.model

import com.example.myfirstapp.Location
import com.example.myfirstapp.Sensor
import com.google.gson.annotations.SerializedName


data class MySensorRawData (

  @SerializedName("timestamp"        ) var timestamp        : String?                     = null,
  @SerializedName("sampling_rate"    ) var samplingRate     : String?                     = null,
  @SerializedName("id"               ) var id               : String?                        = null,
  @SerializedName("sensordatavalues" ) var sensordatavalues : ArrayList<Sensordatavalues> = arrayListOf(),
  @SerializedName("sensor"           ) var sensor           : Sensor?                     = Sensor(),
  @SerializedName("location"         ) var location         : Location?                   = Location()

)