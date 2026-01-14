package com.saionji.mysensor.ui.map

import com.saionji.mysensor.ui.map.model.MapBounds
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap

class MapLibreController(
    private val map: MapLibreMap
) : MapController {

    private var wasCentered = false
    private var viewportListener: ((MapBounds) -> Unit)? = null

    override fun moveTo(lat: Double, lon: Double, zoom: Double?) {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(lat, lon),
                zoom ?: map.cameraPosition.zoom
            )
        )
    }

    override fun centerOnce(lat: Double, lon: Double, zoom: Double) {
        if (wasCentered) return
        wasCentered = true
        moveTo(lat, lon, zoom)
    }

    override fun setOnViewportChangedListener(listener: (MapBounds) -> Unit) {
        viewportListener = listener

        map.addOnCameraIdleListener {
            val bounds = map.projection.visibleRegion.latLngBounds
            listener(
                MapBounds(
                    north = bounds.northEast.latitude,
                    south = bounds.southWest.latitude,
                    east = bounds.northEast.longitude,
                    west = bounds.southWest.longitude,
                    zoom = map.cameraPosition.zoom
                )
            )
        }
    }

    override fun zoomIn() {
        map.animateCamera(
            CameraUpdateFactory.zoomIn()
        )
    }

    override fun zoomOut() {
        map.animateCamera(
            CameraUpdateFactory.zoomOut()
        )
    }

    private var wasInitialCameraSet = false

    override fun setInitialCamera(lat: Double, lon: Double, zoom: Double) {
        if (wasInitialCameraSet) return
        wasInitialCameraSet = true

        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(lat, lon),
                zoom
            )
        )
    }
}
