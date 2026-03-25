package com.saionji.mysensor.shared.ui.map

import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import com.saionji.mysensor.shared.ui.map.model.MapBounds


/**
 * Android implementation of MapController using MapLibre SDK
 *
 * Platform-specific: Works only on Android
 * For iOS: Need to create IOSMapLibreController
 */
class AndroidMapLibreController(
    private val mapLibreMap: MapLibreMap
) : MapController {

    private var viewportListener: ((MapBounds) -> Unit)? = null

    init {
        // Слушатель изменения камеры
        mapLibreMap.addOnCameraIdleListener {
            viewportListener?.invoke(getCurrentBounds())
        }
    }

    override fun moveTo(lat: Double, lon: Double, zoom: Double?) {
        val cameraUpdate = if (zoom != null) {
            CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom)
        } else {
            CameraUpdateFactory.newLatLng(LatLng(lat, lon))
        }
        mapLibreMap.animateCamera(cameraUpdate)
    }

    override fun setOnViewportChangedListener(listener: (MapBounds) -> Unit) {
        viewportListener = listener
    }

    override fun zoomIn() {
        mapLibreMap.animateCamera(CameraUpdateFactory.zoomIn())
    }

    override fun zoomOut() {
        mapLibreMap.animateCamera(CameraUpdateFactory.zoomOut())
    }

    /**
     * Получить текущие границы видимой области
     */
    private fun getCurrentBounds(): MapBounds {
        val bounds = mapLibreMap.projection.visibleRegion.latLngBounds
        return MapBounds(
            north = bounds.latitudeNorth,
            south = bounds.latitudeSouth,
            east = bounds.longitudeEast,
            west = bounds.longitudeWest,
            zoom = mapLibreMap.cameraPosition.zoom
        )
    }
}