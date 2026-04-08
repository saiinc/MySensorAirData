package com.saionji.mysensor.shared.ui.map

import com.saionji.mysensor.shared.ui.map.model.MapBounds
import kotlinx.cinterop.ExperimentalForeignApi
import platform.MapLibre.MLNMapView
import platform.MapLibre.MLNCameraUpdate
import platform.MapLibre.MLNCoordinateBounds

@OptIn(ExperimentalForeignApi::class)
class IosMapController(
    private val mapView: MLNMapView
) : MapController {

    private var viewportListener: ((MapBounds) -> Unit)? = null

    init {
        // Слушатель изменения камеры
        mapView.addDidFinishLoadingMapListener {
            viewportListener?.invoke(getCurrentBounds())
        }
    }

    override fun moveTo(lat: Double, lon: Double, zoom: Double?) {
        val cameraUpdate = if (zoom != null) {
            MLNCameraUpdate(
                targetCoordinate = platform.MapLibre.MLNCoordinateCoordinateMake(lat, lon),
                zoomLevel = zoom
            )
        } else {
            MLNCameraUpdate(
                targetCoordinate = platform.MapLibre.MLNCoordinateCoordinateMake(lat, lon)
            )
        }
        mapView.flyToCamera(cameraUpdate, duration = 1.0, completion = null)
    }

    override fun setOnViewportChangedListener(listener: (MapBounds) -> Unit) {
        viewportListener = listener
    }

    override fun zoomIn() {
        val currentZoom = mapView.zoomLevel
        mapView.setZoomLevel(currentZoom + 1.0, animated = true)
    }

    override fun zoomOut() {
        val currentZoom = mapView.zoomLevel
        mapView.setZoomLevel(currentZoom - 1.0, animated = true)
    }

    private fun getCurrentBounds(): MapBounds {
        val bounds = mapView.visibleCoordinateBounds
        return MapBounds(
            north = bounds.ne.latitude,
            south = bounds.sw.latitude,
            east = bounds.ne.longitude,
            west = bounds.sw.longitude,
            zoom = mapView.zoomLevel
        )
    }
}