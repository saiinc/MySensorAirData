package com.example.myfirstapp.data

import android.content.Context
import com.example.myfirstapp.network.model.SensorService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val mySensorRepository: MySensorRepository

}

class DefaultAppContainer() : AppContainer{
    private val BASE_URL = "https://data.sensor.community/airrohr/v1/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .build()

    private val retrofitService: SensorService by lazy {
        retrofit.create(SensorService::class.java)
    }

    override val mySensorRepository: MySensorRepository by lazy {
        NetworkMySensorRepository(retrofitService)
    }

}