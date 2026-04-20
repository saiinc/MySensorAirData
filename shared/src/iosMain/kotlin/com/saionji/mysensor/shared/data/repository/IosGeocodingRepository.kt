package com.saionji.mysensor.shared.data.repository

import com.saionji.mysensor.shared.domain.model.GeocodingRepository
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class IosGeocodingRepository : GeocodingRepository {

    private val geocoder = CLGeocoder()

    override suspend fun getAddress(lat: Double?, lon: Double?): String {
        if (lat == null || lon == null) return ""

        return suspendCancellableCoroutine { continuation ->
            val location = CLLocation(latitude = lat, longitude = lon)

            geocoder.reverseGeocodeLocation(location) { placemarks, error ->
                val result = when {
                    error != null -> "Ошибка геокодинга"
                    placemarks.isNullOrEmpty() -> "Адрес не найден"
                    else -> formatPlacemark(placemarks.firstOrNull() as CLPlacemark?)
                }

                continuation.resume(result)
            }
        }
    }

    private fun formatPlacemark(placemark: CLPlacemark?): String {
        if (placemark == null) return "Адрес не найден"

        val parts = listOfNotNull(
            placemark.thoroughfare,
            placemark.subThoroughfare,
            placemark.locality
        ).filter { it.isNotBlank() }

        return parts.takeIf { it.isNotEmpty() }?.joinToString(", ")
            ?: "Адрес не найден"
    }
}