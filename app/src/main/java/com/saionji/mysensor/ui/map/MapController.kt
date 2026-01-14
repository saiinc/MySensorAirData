package com.saionji.mysensor.ui.map

import com.saionji.mysensor.ui.map.model.MapBounds

interface MapController {

    /** Переместить камеру */
    fun moveTo(
        lat: Double,
        lon: Double,
        zoom: Double? = null
    )

    /** Центрировать только один раз (например, при старте) */
    fun centerOnce(
        lat: Double,
        lon: Double,
        zoom: Double
    )

    /** Слушать изменение видимой области */
    fun setOnViewportChangedListener(
        listener: (MapBounds) -> Unit
    )

    fun zoomIn()
    fun zoomOut()

    fun setInitialCamera(
        lat: Double,
        lon: Double,
        zoom: Double
    )
}