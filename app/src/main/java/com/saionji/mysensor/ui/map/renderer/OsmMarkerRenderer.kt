package com.saionji.mysensor.ui.map.renderer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.saionji.mysensor.ui.map.model.MapMarkerUiModel
//import com.saionji.mysensor.ui.screens.createColoredCircleDrawable
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
/*
class OsmMarkerRenderer {

    fun render(
        mapView: MapView,
        markers: List<MapMarkerUiModel>,
        onMarkerClick: (MapMarkerUiModel) -> Unit
    ) {
        mapView.overlays.removeAll { it is RadiusMarkerClusterer }

        val clusterer = RadiusMarkerClusterer(mapView.context).apply {
            setRadius(150)
            setMaxClusteringZoomLevel(15)
        }

        markers.forEach { markerUi ->
            val marker = Marker(mapView).apply {
                position = GeoPoint(markerUi.lat, markerUi.lon)
                icon = createColoredCircleDrawable(markerUi.colorInt, mapView.context)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                setOnMarkerClickListener { _, _ ->
                    onMarkerClick(markerUi)
                    true
                }
            }
            clusterer.add(marker)
        }

        mapView.overlays.add(clusterer)
        mapView.invalidate()
        Log.d("OsmRenderer", "markers=${markers.size}, zoom=${mapView.zoomLevelDouble}")
    }
}*/
