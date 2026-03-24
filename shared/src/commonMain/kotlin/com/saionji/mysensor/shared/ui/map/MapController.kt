package com.saionji.mysensor.shared.ui.map

import com.saionji.mysensor.shared.ui.map.model.MapBounds

interface MapController {

    /** Переместить камеру */
    fun moveTo(
        lat: Double,
        lon: Double,
        zoom: Double? = null
    )

    /** Слушать изменение видимой области */
    fun setOnViewportChangedListener(
        listener: (MapBounds) -> Unit
    )

    fun zoomIn()
    fun zoomOut()

}