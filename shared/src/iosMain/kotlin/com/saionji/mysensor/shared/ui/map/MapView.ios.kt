package com.saionji.mysensor.shared.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.saionji.mysensor.shared.domain.model.MapMarker
import com.saionji.mysensor.shared.ui.map.interop.MapLibreWrapper
import platform.Foundation.NSDictionary
import platform.Foundation.NSNumber
import platform.Foundation.NSString
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
    val controller = remember { IosMapController(wrapper) }

    LaunchedEffect(wrapper, controller) {
        controller.setViewportCallback(wrapper)

        wrapper.onMarkerClick = { markerId ->
            onMarkerClick(markerId)
        }

        wrapper.onMapClick = {
            onMapClick()
        }
    }
    
    // Обновляем маркеры при изменении списка
    LaunchedEffect(markers) {
        println("MapView: markers count = ${markers.size}")
        val nsMarkers = markers.map { marker ->
            mapOf(
                "id" to marker.id as NSString,
                "lat" to marker.lat as NSNumber,
                "lon" to marker.lon as NSNumber,
                "colorInt" to marker.colorInt as NSNumber
            ) as NSDictionary
        }
        wrapper.setMarkers(nsMarkers)
    }

    UIKitView(
        factory = { uiView },
        modifier = modifier,
        update = {
            onMapReady(controller)
        }
    )
}
