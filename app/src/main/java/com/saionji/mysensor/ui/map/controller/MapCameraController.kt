package com.saionji.mysensor.ui.map.controller

interface MapCameraController {

    /** Мгновенно или плавно переместить камеру */
    fun moveTo(
        lat: Double,
        lon: Double,
        zoom: Double? = null,
        animated: Boolean = true
    )

    /** Установить зум без смещения центра */
    fun setZoom(zoom: Double, animated: Boolean = true)

    /** Центрировать карту на пользователе (однократно) */
    fun centerOnce(
        lat: Double,
        lon: Double,
        zoom: Double? = null
    )
}