package com.saionji.mysensor.shared.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.MapLibre.MLNMapView
import platform.CoreLocation.CLLocationCoordinate2D

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView(
    modifier: Modifier,
    onMapReady: (MapController) -> Unit
) {
    val mapView = remember {
        MLNMapView(
            frame = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0),
            styleURL = "https://demotiles.maplibre.org/style.json"
        ).apply {
            // Начальная позиция
            setCenterCoordinate(
                CLLocationCoordinate2D(55.7558, 37.6173), // Москва
                zoomLevel = 10.0,
                animated = false
            )
        }
    }

    UIKitView(
        factory = { mapView },
        modifier = modifier,
        update = {
            onMapReady(IosMapController(mapView))
        }
    )
}