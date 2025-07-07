package com.saionji.mysensor.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.util.TypedValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.saionji.mysensor.C
import com.saionji.mysensor.data.SettingsSensor
import com.saionji.mysensor.domain.*
import com.saionji.mysensor.network.model.MySensorRawData
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

@OptIn(FlowPreview::class)
@Composable
fun MapScreen(
    currentLocation: GeoPoint?,
    context: Context,
    sensorList: List<MySensorRawData>,
    settingsItems: State<List<SettingsSensor>>,
    onAreaChanged: (BoundingBox) -> Unit,
    onAddToDashboard: (MySensorRawData, String) -> Unit,
    onRemoveFromDashboard: (MySensorRawData) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val boundingBoxFlow = remember { MutableSharedFlow<BoundingBox>(extraBufferCapacity = 1) }
    val wasCentered = remember { mutableStateOf(false) }
    val mapView = remember { mutableStateOf<MapView?>(null) }

    var lastRequestedBox by remember { mutableStateOf<BoundingBox?>(null) }
    val valueTypes = listOf("PM2.5", "PM10", "temperature", "humidity", "pressure", "noise LAeq")
    var selectedValueType by remember { mutableStateOf(valueTypes[0]) }
    var expanded by remember { mutableStateOf(false) }
    val lastOpenedInfoWindow = remember { mutableStateOf<InfoWindow?>(null) }
    val addressCache = remember { mutableStateMapOf<String, String>() }

    fun BoundingBox.similarTo(other: BoundingBox, threshold: Double = 0.05): Boolean {
        return abs(this.latNorth - other.latNorth) < threshold &&
                abs(this.latSouth - other.latSouth) < threshold &&
                abs(this.lonEast - other.lonEast) < threshold &&
                abs(this.lonWest - other.lonWest) < threshold
    }

    fun BoundingBox.withPadding(padding: Double): BoundingBox {
        return BoundingBox(
            this.latNorth + padding,
            this.lonEast + padding,
            this.latSouth - padding,
            this.lonWest - padding
        )
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context,
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    LaunchedEffect(mapView.value) {
        // Ждём пока карта появится
        val map = mapView.value ?: return@LaunchedEffect
        // Делаем задержку на кадр, чтобы boundingBox успел просчитаться
        snapshotFlow { map.boundingBox }
            .filterNotNull()
            .firstOrNull { it.latNorth != 0.0 && it.latSouth != 0.0 }?.let { bbox ->
                val expandedBox = bbox.withPadding(0.03)
                lastRequestedBox = expandedBox
                onAreaChanged(expandedBox)
            }
    }

    LaunchedEffect(Unit) {
        boundingBoxFlow
            .debounce(700)
            .collect { bbox ->
                val expandedBox = bbox.withPadding(0.03)
                if (lastRequestedBox == null || !expandedBox.similarTo(lastRequestedBox!!)) {
                    lastRequestedBox = expandedBox
                    onAreaChanged(expandedBox)
                }
            }
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
                    setTileSource(TileSourceFactory.MAPNIK)
                    setTilesScaledToDpi(true) // Улучшает читаемость
                    setUseDataConnection(true)
                    setMultiTouchControls(true)
                    minZoomLevel = 6.0
                    maxZoomLevel = 18.0
                    controller.setZoom(10.5)
                    currentLocation?.let { controller.setCenter(it) }
                    mapView.value = this
                }
            },
            update = { view ->
                if (!wasCentered.value && currentLocation != null) {
                    view.controller.setCenter(currentLocation)
                    wasCentered.value = true
                }

                // Обновляем InfoWindow при тапе на карту
                val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                        mapView.value?.let { InfoWindow.closeAllInfoWindowsOn(it) }
                        lastOpenedInfoWindow.value = null
                        return true
                    }

                    override fun longPressHelper(p: GeoPoint?): Boolean = false
                })

                // Удаляем все оверлеи, затем добавим MapEvents и кластер
                view.overlays.clear()
                view.overlays.add(mapEventsOverlay)

                // Создаём кластерер, который сам отключит кластеризацию при зуме > maxClusteringZoomLevel
                val clusterer = RadiusMarkerClusterer(context).apply {
                    val drawable = ContextCompat.getDrawable(context, org.osmdroid.bonuspack.R.drawable.marker_cluster)
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    setRadius(150)
                    setIcon(bitmap)
                    textPaint.color = android.graphics.Color.WHITE
                    textPaint.textSize = 24f
                    mAnchorU = Marker.ANCHOR_CENTER
                    mAnchorV = Marker.ANCHOR_CENTER
                    setMaxClusteringZoomLevel(15) // Автоматическое отключение кластеризации
                }

                // Добавляем маркеры (всегда) в кластерер
                sensorList
                    .filter { it.sensordatavalues.any { v -> v.valueType == selectedValueType } }
                    .take(1000)
                    .forEach { sensor ->
                        val value = sensor.sensordatavalues.find { it.valueType == selectedValueType }?.value ?: 0.0
                        val id = sensor.sensor?.id
                        val color = sensor.sensordatavalues.find { it.valueType == selectedValueType }?.color ?: Color.Gray
                        val lat = sensor.location.latitude
                        val lon = sensor.location.longitude
                        val key = "$lat,$lon"
                        val geoPoint = GeoPoint(lat, lon)
                        val address = addressCache[key] ?: ""


                        val marker = Marker(view).apply {
                            position = geoPoint
                            icon = createColoredCircleDrawable(color.toArgb(), context)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            infoWindow = mapView.value?.let { map ->
                                MapMarkerInfoWindow(
                                    mapView = map,
                                    isAdded = settingsItems.value.any { it.id == id.toString() },
                                    isLimitReached = settingsItems.value.size > C.DASHBOARD_SENSOR_LIMIT,
                                    id = id.toString(),
                                    initialAddress = address,
                                    valueType = selectedValueType,
                                    value = value.toString(),
                                    onAddToDashboard = { sensor, address ->
                                        onAddToDashboard(sensor, address)
                                    },
                                    onRemoveFromDashboard = {
                                        onRemoveFromDashboard(sensor)
                                    },
                                    sensor = sensor
                                )
                            }
                            setOnMarkerClickListener { m, _ ->
                                mapView.value?.let { InfoWindow.closeAllInfoWindowsOn(it) }
                                lastOpenedInfoWindow.value = null

                                val geo = m.position
                                val key = "${geo.latitude},${geo.longitude}"
                                val id = sensor.sensor?.id.toString()
                                val value = sensor.sensordatavalues.find { it.valueType == selectedValueType }?.value ?: 0.0

                                // Проверим, есть ли адрес в кэше
                                val address = addressCache[key]
                                if (address != null) {
                                    m.infoWindow = mapView.value?.let { map ->
                                        MapMarkerInfoWindow(
                                            mapView = map,
                                            isAdded = settingsItems.value.any { it.id == id },
                                            isLimitReached = settingsItems.value.size > C.DASHBOARD_SENSOR_LIMIT,
                                            id = id,
                                            initialAddress = address,
                                            valueType = selectedValueType,
                                            value = value.toString(),
                                            onAddToDashboard = { sensor, address ->
                                                onAddToDashboard(sensor, address)
                                            },
                                            onRemoveFromDashboard = {
                                                onRemoveFromDashboard(sensor)
                                            },
                                            sensor = sensor
                                        )
                                    }
                                    m.showInfoWindow()
                                    lastOpenedInfoWindow.value = m.infoWindow
                                } else {
                                    // Пока нет адреса — покажем пустую строку и начнём асинхронную загрузку
                                    m.infoWindow = mapView.value?.let { map ->
                                        MapMarkerInfoWindow(
                                            mapView = map,
                                            isAdded = settingsItems.value.any { it.id == id },
                                            isLimitReached = settingsItems.value.size > C.DASHBOARD_SENSOR_LIMIT,
                                            id = id,
                                            initialAddress = addressCache[key], // может быть null или ""
                                            valueType = selectedValueType,
                                            value = value.toString(),
                                            onAddToDashboard = { sensor, address ->
                                                onAddToDashboard(sensor, address)
                                            },
                                            onRemoveFromDashboard = {
                                                onRemoveFromDashboard(sensor)
                                            },
                                            sensor = sensor
                                        )
                                    }
                                    m.showInfoWindow()
                                    lastOpenedInfoWindow.value = m.infoWindow

                                    coroutineScope.launch {
                                        val resolved = getAddressFromCoordinates(context, geo.latitude, geo.longitude)
                                        addressCache[key] = resolved

                                        // Обновим InfoWindow с новым адресом
                                        if (m.isInfoWindowShown) {
                                            m.infoWindow = mapView.value?.let { map ->
                                                MapMarkerInfoWindow(
                                                    mapView = map,
                                                    isAdded = settingsItems.value.any { it.id == id },
                                                    isLimitReached = settingsItems.value.size > C.DASHBOARD_SENSOR_LIMIT,
                                                    id = id,
                                                    initialAddress = resolved,
                                                    valueType = selectedValueType,
                                                    value = value.toString(),
                                                    onAddToDashboard = { sensor, address ->
                                                        onAddToDashboard(sensor, address)
                                                    },
                                                    onRemoveFromDashboard = {
                                                        onRemoveFromDashboard(sensor)
                                                    },
                                                    sensor = sensor
                                                )
                                            }
                                            m.showInfoWindow()
                                        }
                                    }
                                }

                                true
                            }
                        }

                        clusterer.add(marker)
                    }

                // Добавляем кластерер как основной слой с маркерами
                view.overlays.add(clusterer)

                // MapListener для обработки зума и скролла (обновление bbox)
                view.setMapListener(object : MapListener {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        if (view.zoomLevelDouble < 8.0) return false
                        view.boundingBox?.let {
                            coroutineScope.launch { boundingBoxFlow.emit(it) }
                        }
                        return false
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        val zoom = view.zoomLevelDouble
                        if (zoom < 6.0) {
                            view.controller.setZoom(6.0)
                            return true
                        } else if (zoom > 18.0) {
                            view.controller.setZoom(18.0)
                            return true
                        }

                        view.boundingBox?.let {
                            coroutineScope.launch { boundingBoxFlow.emit(it) }
                        }
                        return false
                    }
                })

                view.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = {
                currentLocation?.let {
                    mapView.value?.controller?.animateTo(it)
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
                            selectedValueType = type
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

suspend fun getAddressFromCoordinates(context: Context, lat: Double, lon: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                listOfNotNull(
                    address.thoroughfare, // улица
                    address.subThoroughfare, // номер дома
                    address.locality, // город
                    address.countryName // страна
                ).joinToString(", ")
            } else {
                "Unknown address"
            }
        } catch (e: Exception) {
            "Error getting address"
        }
    }
}

