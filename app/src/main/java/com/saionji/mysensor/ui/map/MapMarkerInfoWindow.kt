package com.saionji.mysensor.ui.map

import android.graphics.Color
import android.graphics.PorterDuff
import android.widget.ImageView
import android.widget.TextView
import com.saionji.mysensor.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.infowindow.InfoWindow

class MapMarkerInfoWindow(
    mapView: MapView,
    private val id: String,
    private val lat: Double,
    private val lon: Double,
    private val valueType: String,
    private val value: String,
    private val isAdded: Boolean,
    private val isLimitReached: Boolean,
    private val addressProvider: (Double, Double) -> String?,
    private val onAddToDashboard: (String, String) -> Unit,
    private val onRemoveFromDashboard: (String) -> Unit
) : InfoWindow(R.layout.custom_info_window, mapView) {

    override fun onOpen(item: Any?) {
        val view = mView ?: return

        val address = addressProvider(lat, lon) ?: "…"

        view.findViewById<TextView>(R.id.info_text).text =
            "$valueType: $value\nid: $id"

        view.findViewById<TextView>(R.id.address_text).text = address

        val addButton = view.findViewById<ImageView>(R.id.bookmark_icon)
        var currentIsAdded = isAdded

        if (isLimitReached && !currentIsAdded) {
            addButton.setImageResource(R.drawable.baseline_bookmark_border_24)
            addButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN)
            addButton.isEnabled = false
        } else {
            updateIconState(addButton, currentIsAdded)

            addButton.setOnClickListener {
                currentIsAdded = !currentIsAdded
                updateIconState(addButton, currentIsAdded)

                if (currentIsAdded) {
                    onAddToDashboard(id, address)
                } else {
                    onRemoveFromDashboard(id)
                }
            }
        }
    }

    override fun onClose() {}

    private fun updateIconState(icon: ImageView, added: Boolean) {
        val color = if (added) Color.parseColor("#FE8F52") else Color.GRAY
        icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        icon.isEnabled = true
    }
}