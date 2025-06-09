package com.saionji.mysensor.ui.screens

import android.graphics.Color
import android.graphics.PorterDuff
import android.widget.ImageView
import android.widget.TextView
import com.saionji.mysensor.R
import com.saionji.mysensor.network.model.MySensorRawData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class MapMarkerInfoWindow(
    mapView: MapView,
    private val sensor: MySensorRawData,
    private val isAdded: Boolean,
    private val isLimitReached: Boolean,
    private val id: String,
    initialAddress: String?,
    private val valueType: String,
    private val value: String,
    private val onAddToDashboard: (MySensorRawData, String) -> Unit,
    private val onRemoveFromDashboard: (MySensorRawData) -> Unit
) : InfoWindow(R.layout.custom_info_window, mapView) {

    private var currentAddress = initialAddress ?: ""

    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return
        val view = mView ?: return
        view.findViewById<TextView>(R.id.info_text).text = "$valueType: $value\nid: $id"
        view.findViewById<TextView>(R.id.address_text).text = currentAddress

        val position = marker.position

        if (currentAddress == "") {
            CoroutineScope(Dispatchers.Main).launch {
                val newAddress = getAddressFromCoordinates(view.context, position.latitude, position.longitude)
                currentAddress = newAddress
                view.findViewById<TextView>(R.id.address_text).text = newAddress
            }
        }

        val addButton = view.findViewById<ImageView>(R.id.bookmark_icon)

        var currentIsAdded = isAdded

        if (isLimitReached && !currentIsAdded) {
            // Лимит списка сенсоров достигнут и сенсор ещё не добавлен
            addButton.setImageResource(R.drawable.baseline_bookmark_border_24)
            addButton.setColorFilter(Color.LTGRAY, PorterDuff.Mode.SRC_IN)
            addButton.isEnabled = false
            addButton.setOnClickListener {  } // чтобы не исчезало окно при нажатии на значок
        } else {
            // Обычное поведение
            updateIconState(addButton, currentIsAdded)

            addButton.setOnClickListener {
                currentIsAdded = !currentIsAdded
                updateIconState(addButton, currentIsAdded)

                if (currentIsAdded) {
                    onAddToDashboard(sensor, currentAddress)
                } else {
                    onRemoveFromDashboard(sensor)
                }
            }
        }
    }

    override fun onClose() {
        // ничего
    }

    private fun updateIconState(icon: ImageView, added: Boolean) {
        val color = if (added) {
            Color.parseColor("#FE8F52")
        } else {
            Color.GRAY
        }
        icon.setColorFilter(color, PorterDuff.Mode.SRC_IN)
        icon.isEnabled = true
    }
}