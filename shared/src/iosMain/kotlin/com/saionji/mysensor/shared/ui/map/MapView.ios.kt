package com.saionji.mysensor.shared.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.saionji.mysensor.shared.domain.model.MapMarker
import com.saionji.mysensor.shared.ui.map.interop.MapLibreWrapper
import platform.UIKit.UIView

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@Composable
fun IosMapView(
    modifier: Modifier,
    markers: List<MapMarker> = emptyList(),   // Для маркеров
    onMapReady: (MapController) -> Unit,
    onMarkerClick: (String) -> Unit = {},     // Для кликов
    onMapClick: () -> Unit = {}               // Для кликов
) {
    val wrapper = remember { MapLibreWrapper() }
    val uiView = remember { wrapper.createMapView() }

    UIKitView(
        factory = { uiView },
        modifier = modifier,
        update = {
            onMapReady(IosMapController(wrapper))
        }
    )
}
