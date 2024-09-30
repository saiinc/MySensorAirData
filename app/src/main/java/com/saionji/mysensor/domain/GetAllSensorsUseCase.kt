/*
 * Copyright © Anton Sorokin 2024. All rights reserved
 */

package com.saionji.mysensor.domain

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
                    "P1" -> { it.valueType = "PM10"; it.value = "${it.value}µg/m³" }
                    "P2" -> { it.valueType = "PM2.5"; it.value = "${it.value}µg/m³" }
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
