package com.saionji.mysensor.domain

import com.saionji.mysensor.data.MySensorRepository
import com.saionji.mysensor.shared.domain.model.MapMarker
import kotlin.math.roundToInt

class GetSensorValuesByAreaUseCase(
    private val repository: MySensorRepository
) {

    suspend operator fun invoke(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        selectedType: String
    ): List<MapMarker> {

        val raw = repository.getSensorDataByArea(
            lat1, lon1, lat2, lon2
        )

        return raw.flatMap { sensor ->

            val lat = sensor.lat
            val lon = sensor.lon
            val id = sensor.id

            sensor.measurements.mapNotNull { m ->

                val normalizedType = when (m.valueType) {
                    "P0" -> "PM1"
                    "P1" -> "PM10"
                    "P2" -> "PM2.5"
                    "noise_LAeq" -> "noise LAeq"
                    else -> m.valueType
                }

                if (normalizedType != selectedType) return@mapNotNull null

                val normalizedValue =
                    if (normalizedType == "pressure")
                        (m.value / 100).roundToInt().toDouble()
                    else
                        m.value

                val formattedValue = when (normalizedType) {

                    "PM1",
                    "PM2.5",
                    "PM10" ->
                        "${normalizedValue}µg/m³"

                    "temperature" ->
                        "${normalizedValue.roundToInt()}°C"

                    "humidity" ->
                        "${normalizedValue.roundToInt()}% RH"

                    "noise LAeq" ->
                        "${normalizedValue}dBA"

                    "pressure" ->
                        "${normalizedValue.roundToInt()}hPa"

                    else ->
                        normalizedValue.toString()
                }

                val color = ColorResolver.resolveColorInt(
                    normalizedType,
                    normalizedValue
                )

                MapMarker(
                    id = id,
                    lat = lat,
                    lon = lon,
                    valueType = normalizedType,
                    value = formattedValue,
                    colorInt = color
                )
            }
        }
    }
}