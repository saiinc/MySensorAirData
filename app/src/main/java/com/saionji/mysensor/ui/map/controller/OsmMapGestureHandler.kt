package com.saionji.mysensor.ui.map.controller

import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay

class OsmMapGestureHandler(
    private val onMapTap: () -> Unit
) {
    fun attach(mapView: MapView) {
        mapView.overlays.removeAll { it is MapEventsOverlay }

        mapView.overlays.add(
            MapEventsOverlay(object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                    onMapTap()
                    return true
                }
                override fun longPressHelper(p: GeoPoint?) = false
            })
        )
    }
}