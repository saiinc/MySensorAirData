package com.saionji.mysensor.ui.map.controller

import org.osmdroid.views.MapView

interface MapGestureHandler {

    fun attach(mapView: MapView)
}