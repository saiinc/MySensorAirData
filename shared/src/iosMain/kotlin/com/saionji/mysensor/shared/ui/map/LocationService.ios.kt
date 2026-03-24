package com.saionji.mysensor.shared.ui.map

import com.saionji.mysensor.shared.domain.model.LatLng

class IosLocationService : LocationService {

    override suspend fun getCurrentLocation(callback: (LatLng?) -> Unit) {
        // TODO: Реализовать с CoreLocation для iOS
        // Сейчас просто заглушка
        callback(LatLng(55.7558, 37.6173)) // Москва
    }
}