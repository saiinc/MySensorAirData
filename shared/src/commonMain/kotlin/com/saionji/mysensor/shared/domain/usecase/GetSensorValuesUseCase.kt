package com.saionji.mysensor.shared.domain.usecase

import com.saionji.mysensor.shared.data.MySensor
import com.saionji.mysensor.shared.data.model.SettingsSensor
import com.saionji.mysensor.shared.domain.ColorResolver
import com.saionji.mysensor.shared.domain.repository.MySensorRepository
import kotlin.math.roundToInt

class GetSensorValuesUseCase(
    private val mySensorRepository: MySensorRepository
) {

    private val sensorOrder = mapOf(
        "PM1" to 0,
        "PM2.5" to 1,
        "PM10" to 2,
        "temperature" to 3,
        "humidity" to 4,
        "pressure" to 5,
        "noise LAeq" to 6
    )

    suspend operator fun invoke(device: SettingsSensor): List<MySensor> {

        return mySensorRepository
            .getSensor(device.id)
            .filter { it.valueType != "pressure_at_sealevel" }
            .map { sensor ->

                when (sensor.valueType) {

                    "P0" -> sensor.copy(
                        valueType = "PM1",
                        value = "${sensor.value}µg/m³"
                    )

                    "P1" -> sensor.copy(
                        valueType = "PM10",
                        color = ColorResolver.resolveColorInt("PM10", sensor.value.toDouble()),
                        value = "${sensor.value}µg/m³"
                    )

                    "P2" -> sensor.copy(
                        valueType = "PM2.5",
                        color = ColorResolver.resolveColorInt("PM2.5", sensor.value.toDouble()),
                        value = "${sensor.value}µg/m³"
                    )

                    "temperature" -> sensor.copy(
                        color = ColorResolver.resolveColorInt("temperature", sensor.value.toDouble()),
                        value = "${sensor.value.toDouble().roundToInt()}°C"
                    )

                    "humidity" -> sensor.copy(
                        color = ColorResolver.resolveColorInt("humidity", sensor.value.toDouble()),
                        value = "${sensor.value.toDouble().roundToInt()}% RH"
                    )

                    "noise_LAeq" -> sensor.copy(
                        valueType = "noise LAeq",
                        color = ColorResolver.resolveColorInt("noise LAeq", sensor.value.toDouble()),
                        value = "${sensor.value}dBA"
                    )

                    "pressure" -> sensor.copy(
                        color = ColorResolver.resolveColorInt("pressure", sensor.value.toDouble() / 100),
                        value = "${(sensor.value.toDouble() / 100).roundToInt()}hPA"
                    )

                    else -> sensor
                }
            }
            .sortedBy { sensorOrder[it.valueType] ?: Int.MAX_VALUE }
    }
}