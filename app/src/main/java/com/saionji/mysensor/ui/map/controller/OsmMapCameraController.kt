package com.saionji.mysensor.ui.map.controller

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

class OsmMapCameraController(
    private val mapView: MapView
) : MapCameraController {

    private var wasCentered = false

    override fun moveTo(
        lat: Double,
        lon: Double,
        zoom: Double?,
        animated: Boolean
    ) {
        val point = GeoPoint(lat, lon)

        if (animated) {
            mapView.controller.animateTo(point)
        } else {
            mapView.controller.setCenter(point)
        }

        zoom?.let {
            if (animated) {
                mapView.controller.setZoom(it)
            } else {
                mapView.controller.zoomTo(it)
            }
        }
    }

    override fun setZoom(zoom: Double, animated: Boolean) {
        if (animated) {
            mapView.controller.setZoom(zoom)
        } else {
            mapView.controller.zoomTo(zoom)
        }
    }

    override fun centerOnce(
        lat: Double,
        lon: Double,
        zoom: Double?
    ) {
        if (wasCentered) return
        wasCentered = true

        moveTo(
            lat = lat,
            lon = lon,
            zoom = zoom,
            animated = false
        )
    }
}