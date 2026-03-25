package com.saionji.mysensor.ui.map

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.saionji.mysensor.shared.domain.C
import com.saionji.mysensor.shared.domain.model.MapMarker
import com.saionji.mysensor.shared.ui.map.AndroidMapLibreController
import com.saionji.mysensor.shared.ui.map.MapController
import com.saionji.mysensor.ui.map.renderer.MapLibreMarkerRenderer
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import kotlin.apply


@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    markers: List<MapMarker>,
    onMapReady: (MapController) -> Unit,
    onMarkerClick: (String) -> Unit,
    onMapClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // MapView должен быть remember'нут
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    // Renderer теперь внутри, недоступен снаружи
    val markerRenderer = remember { mutableStateOf<MapLibreMarkerRenderer?>(null) }

    // ✅ Реактивное обновление маркеров
    LaunchedEffect(markers) {
        markerRenderer.value?.showMarkers(markers)
    }

    // 🔁 синхронизация lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    map.setStyle(
                        Style.Builder().fromUri(C.MAP_STYLE_URI)
                    ) {
                        val controller = AndroidMapLibreController(map)

                        // ✅ Создаём renderer внутри
                        val renderer = MapLibreMarkerRenderer(map)
                        markerRenderer.value = renderer

                        // ✅ Показываем начальные маркеры
                        renderer.showMarkers(markers)

                        // Клик по маркеру
                        map.addOnMapClickListener { point ->
                            val screenPoint = map.projection.toScreenLocation(point)

                            val features = map.queryRenderedFeatures(
                                screenPoint,
                                "markers-layer" // ID слоя маркеров
                            )

                            if (features.isNotEmpty()) {
                                val feature = features.first()
                                val id = feature.getStringProperty("id")
                                onMarkerClick(id)
                                true
                            } else {
                                onMapClick()
                                false
                            }
                        }
                        // ✅ Возвращаем только контроллер
                        onMapReady(controller)
                    }

                }
            }
        }
    )
}