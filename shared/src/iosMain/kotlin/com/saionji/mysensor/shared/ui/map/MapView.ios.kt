package com.saionji.mysensor.shared.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.UIKit.UIView

@Composable
fun IosMapView(
    modifier: Modifier,
    onMapReady: (MapController) -> Unit
) {
    val mapView = remember {
        // Вызываем Swift wrapper
        MapLibreWrapper().createMapView()
    }

    UIKitView(
        factory = { mapView },
        modifier = modifier,
        update = {
            onMapReady(IosMapController(mapView))
        }
    )
}