package com.saionji.mysensor.ui.map.engine

import com.saionji.mysensor.ui.map.model.MapBounds
import com.saionji.mysensor.ui.map.MapController
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/*class OsmMapController(
    private val mapView: MapView
) : MapController {

    private var onViewportChanged: ((MapBounds) -> Unit)? = null
    private var isReady = false

    override fun getVisibleBounds(): MapBounds? {
        val box = mapView.boundingBox ?: return null
        return MapBounds(
            north = box.latNorth,
            south = box.latSouth,
            east = box.lonEast,
            west = box.lonWest
        )
    }

    override fun setOnViewportChangedListener(
        listener: (MapBounds) -> Unit
    ) {
        onViewportChanged = listener

        mapView.setMapListener(object : MapListener {

            override fun onScroll(event: ScrollEvent?): Boolean {
                emitBounds()
                return false
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                emitBounds()
                return false
            }
        })
    }

    override fun moveTo(lat: Double, lon: Double, zoom: Double?) {
        zoom?.let { mapView.controller.setZoom(it) }
        mapView.controller.animateTo(GeoPoint(lat, lon))

        mapView.post {
            isReady = true
        }
    }

    private fun emitBounds() {
        if (!isReady) return
        val bounds = getVisibleBounds() ?: return
        onViewportChanged?.invoke(bounds)
    }
}*/