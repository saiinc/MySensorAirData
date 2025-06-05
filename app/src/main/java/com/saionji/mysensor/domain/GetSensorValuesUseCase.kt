package com.saionji.mysensor.domain

import androidx.compose.ui.graphics.Color
import com.saionji.mysensor.data.MySensor
import com.saionji.mysensor.data.MySensorRepository
import com.saionji.mysensor.data.SettingsSensor
import kotlin.math.roundToInt

class GetSensorValuesUseCase(private val mySensorRepository: MySensorRepository) {
    suspend operator fun invoke(device: SettingsSensor): List<MySensor> {
        val singleSensor = mySensorRepository.getSensor(device.id).toMutableList()
        singleSensor.forEach {
            when (it.valueType) {
                "P0" -> {
                    it.valueType = "PM1"
                    it.value = "${it.value}µg/m³"
                }
                "P1" -> {
                    it.valueType = "PM10"
                    it.color = Color(interpolateColor(it.value.toDouble(), PM10_COLOR_RANGES))
                    it.value = "${it.value}µg/m³"
                }
                "P2" -> {
                    it.valueType = "PM2.5";
                    it.color = Color(interpolateColor(it.value.toDouble(), PM25_COLOR_RANGES))
                    it.value = "${it.value}µg/m³"
                }
                "temperature" -> {
                    it.color = Color(interpolateColor(it.value.toDouble(), TEMPERATURE_COLOR_RANGES))
                    it.value = "${it.value.toDouble().roundToInt()}°C"
                }
                "humidity" -> {
                    it.color = Color(interpolateColor(it.value.toDouble(), HUMIDITY_COLOR_RANGES))
                    it.value = "${it.value.toDouble().roundToInt()}% RH"
                }
                "noise_LAeq" -> {
                    it.valueType = "noise LAeq"
                    it.color = Color(interpolateColor(it.value.toDouble(), NOISE_COLOR_RANGES))
                    it.value = "${it.value}dBA"
                }
                "pressure" -> {
                    it.color = Color(interpolateColor(it.value.toDouble()/100, PRESSURE_COLOR_RANGES))
                    it.value = "${it.value.toDouble().div(100).roundToInt()}hPA"
                }
                "pressure_at_sealevel" -> singleSensor.remove(it)
            }
        }
        return singleSensor
    }
}