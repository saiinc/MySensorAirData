/*
 * Copyright © Anton Sorokin 2024. All rights reserved
 */

package com.saionji.mysensor.domain

import androidx.compose.ui.graphics.Color
import com.saionji.mysensor.data.MyDevice
import com.saionji.mysensor.data.MySensor
import com.saionji.mysensor.data.MySensorRepository
import com.saionji.mysensor.data.SettingsSensor
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.roundToInt

class GetAllSensorsUseCase(private val mySensorRepository: MySensorRepository) {

    suspend operator fun invoke(devices: List<SettingsSensor>): List<MyDevice> {
        val allDevices = mutableListOf<MyDevice>()
        val devicesClone = devices.toList()

        // Функция для линейной интерполяции цвета на основе значения PM2.5 или PM10
        fun interpolateColor(pmValue: Double, pmType: String): Int {
            // Цветовые диапазоны для PM2.5
            val colorsPM25 = listOf(
                Triple(0.0, 12.0, Triple(0x00, 0xE4, 0x00)),     // 0-12: зеленый (#00E400)
                Triple(12.1, 35.4, Triple(0xFF, 0xE6, 0x00)),   // 12.1-35.4: желтый (#FFE600)
                Triple(35.5, 55.4, Triple(0xFF, 0x7E, 0x00)),   // 35.5-55.4: оранжевый (#FF7E00)
                Triple(55.5, 150.4, Triple(0xFE, 0x00, 0x00)),  // 55.5-150.4: красный (#FE0000)
                Triple(150.5, 250.4, Triple(0x98, 0x00, 0x4B)), // 150.5-250.4: фиолетовый (#98004B)
                Triple(250.5, 500.0, Triple(0x7E, 0x00, 0x23))  // 250.5+: бордовый (#7E0023)
            )

            // Цветовые диапазоны для PM10
            val colorsPM10 = listOf(
                Triple(0.0, 54.0, Triple(0x00, 0xE4, 0x00)),    // 0-54: зеленый (#00E400)
                Triple(55.0, 154.0, Triple(0xFF, 0xE6, 0x00)),  // 55-154: желтый (#FFE600)
                Triple(155.0, 254.0, Triple(0xFF, 0x7E, 0x00)), // 155-254: оранжевый (#FF7E00)
                Triple(255.0, 354.0, Triple(0xFE, 0x00, 0x00)), // 255-354: красный (#FE0000)
                Triple(355.0, 424.0, Triple(0x98, 0x00, 0x4B)), // 355-424: фиолетовый (#98004B)
                Triple(425.0, 500.0, Triple(0x7E, 0x00, 0x23))  // 425+: бордовый (#7E0023)
            )

            // Линейная интерполяция между двумя значениями цвета
            fun linearInterpolate(value: Double, start: Double, end: Double, colorStart: Int, colorEnd: Int): Int {
                return ((colorStart + (value - start) / (end - start) * (colorEnd - colorStart)).roundToInt()).coerceIn(0, 255)
            }

            // Выбираем нужные диапазоны в зависимости от типа данных (PM2.5 или PM10)
            val colors = when (pmType) {
                "PM2.5" -> colorsPM25
                "PM10" -> colorsPM10
                else -> throw IllegalArgumentException("Unknown PM type: $pmType")
            }

            // Найдем интервал, в который попадает значение PM, и выполним интерполяцию цвета
            for (i in 0 until colors.size - 1) {
                val (pmStart, pmEnd, colorStart) = colors[i]
                val (_, _, colorEnd) = colors[i + 1]

                if (pmValue in pmStart..pmEnd) {
                    val r = linearInterpolate(pmValue, pmStart, pmEnd, colorStart.first, colorEnd.first)
                    val g = linearInterpolate(pmValue, pmStart, pmEnd, colorStart.second, colorEnd.second)
                    val b = linearInterpolate(pmValue, pmStart, pmEnd, colorStart.third, colorEnd.third)

                    // Возвращаем цвет в формате ARGB (альфа = 255)
                    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
            }

            // Если значение PM больше максимального, возвращаем последний цвет (бордовый)
            val lastColor = colors.last().third
            return (0xFF shl 24) or (lastColor.first shl 16) or (lastColor.second shl 8) or lastColor.third
        }

        if ((devicesClone.size == 1) && (devices[0].id == "")) {
            allDevices.add(MyDevice(
                id = "",
                description = "",
                deviceSensors = listOf(MySensor(valueType = "Please tap the settings icon to add your sensor IDs.", value = ""))))
            return allDevices
        }

        for (device in devicesClone) {
            val singleSensor = mySensorRepository.getSensor(device.id)
            val singleSensorCopy = CopyOnWriteArrayList(singleSensor)

            singleSensorCopy.forEach {
                when (it.valueType) {
                    "P1" -> {
                        it.valueType = "PM10"
                        it.color = Color(interpolateColor(it.value!!.toDouble(), "PM10"))
                        it.value = "${it.value}µg/m³"
                    }
                    "P2" -> {
                        it.valueType = "PM2.5";
                        it.color = Color(interpolateColor(it.value!!.toDouble(), "PM2.5"))
                        it.value = "${it.value}µg/m³"
                    }
                    "temperature" -> it.value = "${it.value?.toDouble()?.roundToInt()}°C"
                    "humidity" -> it.value = "${it.value?.toDouble()?.roundToInt()}% RH"
                    "noise_LAeq" -> { it.valueType = "noise LAeq"; it.value = "${it.value}dBA" }
                    "pressure" -> it.value = "${it.value?.toDouble()?.div(100)?.roundToInt()}hPA"
                    "pressure_at_sealevel" -> singleSensorCopy.remove(it)
                }
            }

            allDevices.add(MyDevice(id = device.id, description = device.description, deviceSensors = singleSensorCopy))
        }

        return allDevices
    }
}
