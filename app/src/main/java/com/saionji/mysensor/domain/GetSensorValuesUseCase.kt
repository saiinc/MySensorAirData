package com.saionji.mysensor.domain

import androidx.compose.ui.graphics.Color
import com.saionji.mysensor.data.MySensor
import com.saionji.mysensor.data.MySensorRepository
import com.saionji.mysensor.data.SettingsSensor
import kotlin.math.roundToInt

class GetSensorValuesUseCase(private val mySensorRepository: MySensorRepository) {
    suspend operator fun invoke(device: SettingsSensor): List<MySensor> {
        val singleSensor = mySensorRepository.getSensor(device.id).toMutableList()

        // Универсальная функция для интерполяции цвета на основе значения и списка цветовых диапазонов
        fun interpolateColor(value: Double, ranges: List<Triple<Double, Double, Triple<Int, Int, Int>>>): Int {
            // Линейная интерполяция между двумя значениями цвета
            fun linearInterpolate(value: Double, start: Double, end: Double, colorStart: Int, colorEnd: Int): Int {
                return ((colorStart + (value - start) / (end - start) * (colorEnd - colorStart)).roundToInt()).coerceIn(0, 255)
            }

            // Если значение меньше минимального, возвращаем первый цвет
            if (value < ranges.first().first) {
                val firstColor = ranges.first().third
                return (0xFF shl 24) or (firstColor.first shl 16) or (firstColor.second shl 8) or firstColor.third
            }

            // Если значение больше максимального, возвращаем последний цвет
            if (value > ranges.last().second) {
                val lastColor = ranges.last().third
                return (0xFF shl 24) or (lastColor.first shl 16) or (lastColor.second shl 8) or lastColor.third
            }

            // Найдем интервал, в который попадает значение, и выполним интерполяцию цвета
            for (i in 0 until ranges.size - 1) {
                val (start, end, colorStart) = ranges[i]
                val (_, _, colorEnd) = ranges[i + 1]

                if (value in start..end) {
                    val r = linearInterpolate(value, start, end, colorStart.first, colorEnd.first)
                    val g = linearInterpolate(value, start, end, colorStart.second, colorEnd.second)
                    val b = linearInterpolate(value, start, end, colorStart.third, colorEnd.third)

                    // Возвращаем цвет в формате ARGB (альфа = 255)
                    return (0xFF shl 24) or (r shl 16) or (g shl 8) or b
                }
            }

            // На всякий случай, если значение не попало ни в один диапазон (но этого не должно произойти)
            return 0xFF000000.toInt() // Черный цвет по умолчанию
        }

        singleSensor.forEach {
            when (it.valueType) {
                "P0" -> {
                    it.valueType = "PM1"
                    it.value = "${it.value}µg/m³"
                }
                "P1" -> {
                    it.valueType = "PM10"
                    it.color = Color(interpolateColor(it.value!!.toDouble(), listOf(
                        Triple(0.0, 54.99, Triple(0x00, 0xE4, 0x00)),    // 0-54: зеленый (#00E400)
                        Triple(55.0, 154.99, Triple(0xFF, 0xE6, 0x00)),  // 55-154: желтый (#FFE600)
                        Triple(155.0, 254.99, Triple(0xFF, 0x7E, 0x00)), // 155-254: оранжевый (#FF7E00)
                        Triple(255.0, 354.99, Triple(0xFE, 0x00, 0x00)), // 255-354: красный (#FE0000)
                        Triple(355.0, 424.99, Triple(0x98, 0x00, 0x4B)), // 355-424: фиолетовый (#98004B)
                        Triple(425.0, 500.0, Triple(0x7E, 0x00, 0x23))  // 425+: бордовый (#7E0023)
                    )))
                    it.value = "${it.value}µg/m³"
                }
                "P2" -> {
                    it.valueType = "PM2.5";
                    it.color = Color(interpolateColor(it.value!!.toDouble(), listOf(
                        Triple(0.0, 12.09, Triple(0x00, 0xE4, 0x00)),     // 0-12: зеленый (#00E400)
                        Triple(12.1, 35.49, Triple(0xFF, 0xE6, 0x00)),   // 12.1-35.4: желтый (#FFE600)
                        Triple(35.5, 55.49, Triple(0xFF, 0x7E, 0x00)),   // 35.5-55.4: оранжевый (#FF7E00)
                        Triple(55.5, 150.49, Triple(0xFE, 0x00, 0x00)),  // 55.5-150.4: красный (#FE0000)
                        Triple(150.5, 250.49, Triple(0x98, 0x00, 0x4B)), // 150.5-250.4: фиолетовый (#98004B)
                        Triple(250.5, 500.0, Triple(0x7E, 0x00, 0x23))  // 250.5+: бордовый (#7E0023)
                    )))
                    it.value = "${it.value}µg/m³"
                }
                "temperature" -> {
                    it.color = Color(interpolateColor(it.value!!.toDouble(), listOf(
                        Triple(-20.0, -10.0, Triple(0x40, 0x50, 0xB0)), // Синий
                        Triple(-10.0, 0.0, Triple(0x56, 0x79, 0xF9)),   // Голубой
                        Triple(0.0, 10.0, Triple(0x55, 0xCB, 0xD9)),    // Зеленовато-голубой
                        Triple(10.0, 20.0, Triple(0xA2, 0xCF, 0x4A)),   // Зеленый
                        Triple(20.0, 30.0, Triple(0xFE, 0xDB, 0x64)),   // Желтый
                        Triple(30.0, 40.0, Triple(0xFE, 0x8F, 0x52)),   // Оранжевый
                        Triple(40.0, 50.0, Triple(0xE6, 0x38, 0x0F))    // Красный
                    )))
                    it.value = "${it.value?.toDouble()?.roundToInt()}°C"
                }
                "humidity" -> {
                    it.color = Color(interpolateColor(it.value!!.toDouble(), listOf(
                        Triple(0.0, 20.0, Triple(0xC4, 0x1A, 0x0A)),   // Красный
                        Triple(20.0, 40.0, Triple(0xF4, 0x7A, 0x0B)),  // Оранжевый
                        Triple(40.0, 60.0, Triple(0xF4, 0xE6, 0x0B)),  // Желтый
                        Triple(60.0, 80.0, Triple(0xAF, 0xF4, 0x74)),  // Зеленый
                        Triple(80.0, 100.0, Triple(0x6D, 0xBC, 0xFF)), // Голубой
                        Triple(100.0, 120.0, Triple(0x00, 0x52, 0x8F)) // Синий
                    )))
                    it.value = "${it.value?.toDouble()?.roundToInt()}% RH"
                }
                "noise_LAeq" -> {
                    it.valueType = "noise LAeq"
                    it.color = Color(interpolateColor(it.value!!.toDouble(), listOf(
                        Triple(0.0, 20.0, Triple(0x00, 0x52, 0x8F)),   // Синий
                        Triple(20.0, 40.0, Triple(0x6D, 0xBC, 0xFF)),  // Голубой
                        Triple(40.0, 60.0, Triple(0xAF, 0xF4, 0x74)),  // Зеленый
                        Triple(60.0, 80.0, Triple(0xF4, 0xE6, 0x0B)),  // Желтый
                        Triple(80.0, 100.0, Triple(0xF4, 0x7A, 0x0B)), // Оранжевый
                        Triple(100.0, 120.0, Triple(0xC4, 0x1A, 0x0A)) // Красный
                    )))
                    it.value = "${it.value}dBA"
                }
                "pressure" -> {
                    it.color = Color(interpolateColor(it.value!!.toDouble()/100, listOf(
                        Triple(926.0, 948.0, Triple(0xDD, 0x2E, 0x97)),  // Розовый
                        Triple(948.0, 970.0, Triple(0x6B, 0x3B, 0x8F)),  // Фиолетовый
                        Triple(970.0, 991.5, Triple(0x29, 0x79, 0xB9)),  // Синий
                        Triple(991.5, 1013.0, Triple(0x02, 0xB9, 0xED)), // Голубой
                        Triple(1013.0, 1035.0, Triple(0x13, 0xAE, 0x52)),// Зеленый
                        Triple(1035.0, 1057.0, Triple(0xC9, 0xD8, 0x41)),// Желтый
                        Triple(1057.0, 1078.5, Triple(0xFA, 0xD6, 0x35)),// Оранжевый
                        Triple(1078.5, 1100.0, Triple(0xF0, 0xA0, 0x3D)),// Коричневый
                        Triple(1100.0, 1120.0, Triple(0x89, 0x27, 0x25)) // Красный
                    )))
                    it.value = "${it.value?.toDouble()?.div(100)?.roundToInt()}hPA"
                }
                "pressure_at_sealevel" -> singleSensor.remove(it)
            }
        }
        return singleSensor
    }
}