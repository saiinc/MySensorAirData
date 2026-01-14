package com.saionji.mysensor.ui.map.model

fun MapBounds.withPadding(padding: Double): MapBounds =
    copy(
        north = north + padding,
        south = south - padding,
        east = east + padding,
        west = west - padding
    )