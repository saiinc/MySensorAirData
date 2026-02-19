package com.saionji.mysensor.domain

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
                    it.color = ColorResolver.resolveColorInt(it.valueType, it.value.toDouble())
                    it.value = "${it.value}µg/m³"
                }
                "P2" -> {
                    it.valueType = "PM2.5";
                    it.color = ColorResolver.resolveColorInt(it.valueType, it.value.toDouble())
                    it.value = "${it.value}µg/m³"
                }
                "temperature" -> {
                    it.color = ColorResolver.resolveColorInt(it.valueType, it.value.toDouble())
                    it.value = "${it.value.toDouble().roundToInt()}°C"
                }
                "humidity" -> {
                    it.color = ColorResolver.resolveColorInt(it.valueType, it.value.toDouble())
                    it.value = "${it.value.toDouble().roundToInt()}% RH"
                }
                "noise_LAeq" -> {
                    it.valueType = "noise LAeq"
                    it.color = ColorResolver.resolveColorInt(it.valueType, it.value.toDouble())
                    it.value = "${it.value}dBA"
                }
                "pressure" -> {
                    it.color = ColorResolver.resolveColorInt(it.valueType, it.value.toDouble().div(100))
                    it.value = "${it.value.toDouble().div(100).roundToInt()}hPA"
                }
                "pressure_at_sealevel" -> singleSensor.remove(it)
            }
        }
        return singleSensor
    }
}