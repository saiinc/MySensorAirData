package com.saionji.mysensor.shared.domain.model

data class MapMeasurement(
    var valueType: String,   // PM2.5 / PM10 / temperature
    var value: Double
)