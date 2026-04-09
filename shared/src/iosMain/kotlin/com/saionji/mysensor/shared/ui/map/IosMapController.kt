package com.saionji.mysensor.shared.ui.map

import com.saionji.mysensor.shared.ui.map.model.MapBounds
import com.saionji.mysensor.shared.ui.map.interop.MapLibreWrapper

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
class IosMapController(
    private val wrapper: MapLibreWrapper
) : MapController {

    private var viewportListener: ((MapBounds) -> Unit)? = null

    override fun moveTo(lat: Double, lon: Double, zoom: Double?) {
        val z = zoom ?: wrapper.getZoom()
        wrapper.moveTo(lat, lon, z)
    }

    override fun setOnViewportChangedListener(listener: (MapBounds) -> Unit) {
        viewportListener = listener
    }

    override fun zoomIn() {
        wrapper.zoomIn()
    }

    override fun zoomOut() {
        wrapper.zoomOut()
    }
}
