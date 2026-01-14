package com.saionji.mysensor.ui.map.renderer

import android.content.Context
import com.saionji.mysensor.ui.map.model.MapMarkerUiModel
import org.osmdroid.views.MapView

interface MapMarkerRenderer {
    fun render(
        mapView: MapView,
        markers: List<MapMarkerUiModel>,
        context: Context,
        onMarkerClick: (MapMarkerUiModel) -> Unit
    )
}