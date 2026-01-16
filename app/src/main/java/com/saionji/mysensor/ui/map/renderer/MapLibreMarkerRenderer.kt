package com.saionji.mysensor.ui.map.renderer

import com.saionji.mysensor.MapFonts
import com.saionji.mysensor.ui.map.model.MapMarkerUiModel
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonOptions
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

class MapLibreMarkerRenderer(
    private val map: MapLibreMap
) {

    private val sourceId = "sensors-source"
    private val markerLayerId = "markers-layer"
    private val clusterLayerId = "clusters-layer"
    private val clusterCountLayerId = "clusters-count-layer"

    fun showMarkers(markers: List<MapMarkerUiModel>) {
        val style = map.style ?: return

        val features = markers.map {
            Feature.fromGeometry(
                Point.fromLngLat(it.lon, it.lat)
            ).apply {
                addStringProperty("id", it.id)
                addStringProperty("value", it.value.toString())
                addStringProperty("color", colorIntToHex(it.colorInt))
            }
        }

        val collection = FeatureCollection.fromFeatures(features)

        val source = style.getSourceAs<GeoJsonSource>(sourceId)
            ?: GeoJsonSource(
                sourceId,
                collection,
                GeoJsonOptions()
                    .withCluster(true)
                    .withClusterRadius(60)
                    .withClusterMaxZoom(14)
            ).also {
                style.addSource(it)
            }

        source.setGeoJson(collection)

        if (style.getLayer(clusterLayerId) == null) {
            style.addLayer(
                CircleLayer(clusterLayerId, sourceId).withProperties(
                    PropertyFactory.circleColor("#444444"),
                    PropertyFactory.circleRadius(
                        Expression.step(
                            Expression.get("point_count"),
                            14f,
                            Expression.stop(10, 18f),
                            Expression.stop(50, 24f)
                        )
                    ),
                    PropertyFactory.circleOpacity(0.8f)
                ).withFilter(
                    Expression.has("point_count")
                )
            )
        }

        if (style.getLayer(clusterCountLayerId) == null) {
            style.addLayerAbove(
                SymbolLayer(clusterCountLayerId, sourceId).withProperties(
                    PropertyFactory.textField(Expression.get("point_count")),
                    PropertyFactory.textSize(12f),
                    PropertyFactory.textColor("#FFFFFF"),
                    PropertyFactory.textAllowOverlap(true),
                    PropertyFactory.textIgnorePlacement(true),
                    PropertyFactory.textFont(MapFonts.DEFAULT),
                ).withFilter(
                    Expression.has("point_count")
                ), clusterLayerId
            )
        }

        if (style.getLayer(markerLayerId) == null) {
            style.addLayerAbove(
                CircleLayer(markerLayerId, sourceId).withProperties(
                    PropertyFactory.circleRadius(6f),
                    PropertyFactory.circleColor(Expression.get("color")),
                    PropertyFactory.circleOpacity(0.9f),
                    PropertyFactory.circleStrokeColor("#404040"),
                    PropertyFactory.circleStrokeWidth(1f)
                ).withFilter(
                    Expression.not(Expression.has("point_count"))
                ), clusterCountLayerId
            )
        }
    }

    fun clear() {
        map.style?.let { style ->
            style.removeLayer(markerLayerId)
            style.removeSource(sourceId)
        }
    }
    private fun colorIntToHex(colorInt: Int): String {
        val r = (colorInt shr 16) and 0xFF
        val g = (colorInt shr 8) and 0xFF
        val b = colorInt and 0xFF
        return String.format("#%02X%02X%02X", r, g, b)
    }
}
