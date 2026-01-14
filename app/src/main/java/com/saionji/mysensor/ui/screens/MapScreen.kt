package com.saionji.mysensor.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.util.Log
import android.util.TypedValue
import android.view.ViewTreeObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.preference.PreferenceManager
import com.saionji.mysensor.C
import com.saionji.mysensor.domain.model.LatLng
import com.saionji.mysensor.data.SettingsSensor
import com.saionji.mysensor.domain.*
import com.saionji.mysensor.domain.model.LatLngBounds
import com.saionji.mysensor.ui.map.model.MapBounds
import com.saionji.mysensor.ui.map.model.withPadding
import com.saionji.mysensor.network.model.MySensorRawData
import com.saionji.mysensor.ui.map.MapController
import com.saionji.mysensor.ui.map.MapViewModel
import com.saionji.mysensor.ui.map.MarkerPopup
import com.saionji.mysensor.ui.map.controller.MapCameraController
import com.saionji.mysensor.ui.map.controller.OsmMapCameraController
import com.saionji.mysensor.ui.map.controller.OsmMapGestureHandler
import com.saionji.mysensor.ui.map.model.SelectedMarkerUi
//import com.saionji.mysensor.ui.map.renderer.OsmMarkerRenderer
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.util.Locale
import kotlin.math.abs

/*@OptIn(FlowPreview::class)
@Composable
fun MapScreen(
    mapViewModel: MapViewModel,
    currentLocation: LatLng?,
    context: Context,
    settingsItems: State<List<SettingsSensor>>,
    onAddToDashboard: (String, String) -> Unit,
    onRemoveFromDashboard: (String) -> Unit
) {
    val mapView = remember { mutableStateOf<MapView?>(null) }
    val valueTypes = listOf("PM2.5", "PM10", "temperature", "humidity", "pressure", "noise LAeq")
    val selectedValueType by mapViewModel.selectedValueType.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val mapUiState by mapViewModel.mapUiState.collectAsState()
    val selectedMarker by mapViewModel.selectedMarker.collectAsState()
    val addressMap by mapViewModel.addresses.collectAsState()
    val mapController = remember { mutableStateOf<MapController?>(null) }
    val wasCentered = rememberSaveable { mutableStateOf(false) }
    val markerRenderer = remember { OsmMarkerRenderer() }
    val gestureHandler = remember { OsmMapGestureHandler(
            onMapTap = { mapViewModel.clearSelectedMarker() }
        )
    }
    val cameraController = remember { mutableStateOf<MapCameraController?>(null) }

    val markers = when (mapUiState) {
        is MapViewModel.MapUiState.Success ->
            (mapUiState as MapViewModel.MapUiState.Success).markers
        else -> emptyList()
    }
/*
    LaunchedEffect(mapController.value) {
        val controller = mapController.value ?: return@LaunchedEffect

        controller.setOnViewportChangedListener { bounds ->
            mapViewModel.onViewportChanged(
                bounds.withPadding(0.03)
            )
        }
    }*/

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val gradientRanges = when (selectedValueType) {
            "PM2.5" -> PM25_COLOR_RANGES
            "PM10" -> PM10_COLOR_RANGES
            "temperature" -> TEMPERATURE_COLOR_RANGES
            "humidity" -> HUMIDITY_COLOR_RANGES
            "pressure" -> PRESSURE_COLOR_RANGES
            "noise LAeq" -> NOISE_COLOR_RANGES
            else -> PM25_COLOR_RANGES // значение по умолчанию
        }
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapView.value = this
                    setTileSource(TileSourceFactory.MAPNIK)
                    setTilesScaledToDpi(true) // Улучшает читаемость
                    setMultiTouchControls(true)
                    minZoomLevel = 6.0
                    maxZoomLevel = 18.0

                    cameraController.value = OsmMapCameraController(this)
                }
            },
            update = { mapView ->

                // 1. Центрирование
                currentLocation?.let {
                    cameraController.value?.centerOnce(
                        lat = it.lat,
                        lon = it.lon,
                        zoom = 10.5
                    )
                }

                // 2. Жесты карты (tap / long tap)
                gestureHandler.attach(mapView)

            },
            modifier = Modifier.fillMaxSize()

        )

        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner, mapView.value) {
            val mv = mapView.value

            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> mv?.onResume()
                    Lifecycle.Event.ON_PAUSE -> mv?.onPause()
                    Lifecycle.Event.ON_DESTROY -> mv?.onDetach()
                    else -> {}
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        LaunchedEffect(mapView.value, markers) {
            val mv = mapView.value ?: return@LaunchedEffect
            if (markers.isEmpty()) return@LaunchedEffect

            mv.post {
                markerRenderer.render(
                    mapView = mv,
                    markers = markers,
                    onMarkerClick = { markerUi ->
                        mapViewModel.onMarkerSelected(
                            SelectedMarkerUi(
                                id = markerUi.id,
                                lat = markerUi.lat,
                                lon = markerUi.lon,
                                valueType = markerUi.valueType,
                                value = markerUi.value.toString()
                            )
                        )
                    }
                )
            }
        }

        selectedMarker?.let { marker ->
            val key = "${marker.lat},${marker.lon}"
            val address = addressMap[key]

            MarkerPopup(
                marker = marker,
                address = address,
                isAdded = settingsItems.value.any { it.id == marker.id },
                isLimitReached = settingsItems.value.size > C.DASHBOARD_SENSOR_LIMIT,
                onClose = { mapViewModel.clearSelectedMarker() },
                onAdd = { onAddToDashboard(marker.id, address ?: "") },
                onRemove = { onRemoveFromDashboard(marker.id) }
            )
        }



        IconButton(
            onClick = {
                currentLocation?.let {
                    cameraController.value?.moveTo(
                        lat = it.lat,
                        lon = it.lon,
                        //zoom = 14.0
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Центрировать по геопозиции",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        VerticalColorBar(
            colorRanges = gradientRanges,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .width(6.dp)
                .height(180.dp)
                .background(Color.White, shape = RoundedCornerShape(4.dp))
                //.border(1.dp, Color.Gray, shape = RoundedCornerShape(4.dp))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Button(
                onClick = { expanded = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = selectedValueType,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                valueTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type) },
                        onClick = {
                            mapViewModel.setSelectedValueType(type)
                            expanded = false
                        }
                    )
                }
            }
        }

    }
}

fun createColoredCircleDrawable(color: Int, context: Context): Drawable {
    val displayMetrics = context.resources.displayMetrics

    val radiusDp = 12f           // Диаметр будет 24dp (удобно)
    val strokeWidthDp = 2f       // Граница — 2dp

    val radiusPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radiusDp, displayMetrics)
    val strokeWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, strokeWidthDp, displayMetrics)

    val diameterPx = (radiusPx * 2).toInt()
    val bitmap = Bitmap.createBitmap(diameterPx, diameterPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Заливка круга
    val fillPaint = Paint().apply {
        isAntiAlias = true
        this.color = color
        style = Paint.Style.FILL
    }

    // Обводка круга
    val strokePaint = Paint().apply {
        isAntiAlias = true
        this.color = Color.Black.toArgb()
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidthPx
    }

    canvas.drawCircle(
        radiusPx,
        radiusPx,
        radiusPx - strokeWidthPx / 2f,
        fillPaint
    )
    canvas.drawCircle(
        radiusPx,
        radiusPx,
        radiusPx - strokeWidthPx / 2f,
        strokePaint
    )

    return BitmapDrawable(context.resources, bitmap)
}

*/