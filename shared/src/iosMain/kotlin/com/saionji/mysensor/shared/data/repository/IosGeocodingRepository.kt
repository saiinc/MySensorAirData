package com.saionji.mysensor.shared.data.repository

import com.saionji.mysensor.shared.domain.model.GeocodingRepository
import com.saionji.mysensor.shared.generated.resources.Res
import com.saionji.mysensor.shared.generated.resources.address_not_found
import com.saionji.mysensor.shared.generated.resources.geocoding_error
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.getString
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLPlacemark
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class IosGeocodingRepository : GeocodingRepository {

    private val geocoder = CLGeocoder()

    override suspend fun getAddress(lat: Double?, lon: Double?): String {
        if (lat == null || lon == null) return ""

        // Ждем результат колбэка от Apple Geocoder
        val placemark = suspendCancellableCoroutine { continuation ->
            val location = CLLocation(latitude = lat, longitude = lon)
            geocoder.reverseGeocodeLocation(location) { placemarks, error ->
                if (error != null) {
                    continuation.resume(null) // Передаем null, чтобы обработать ошибку
                } else {
                    continuation.resume(placemarks?.firstOrNull() as? CLPlacemark)
                }
            }
        }

        // Теперь мы снова в suspend-контексте, вызываем форматирование
        return if (placemark == null) {
            getString(Res.string.geocoding_error)
        } else {
            formatPlacemark(placemark)
        }
    }

    private suspend fun formatPlacemark(placemark: CLPlacemark?): String {
        if (placemark == null) return getString(Res.string.address_not_found) //"Адрес не найден"

        val parts = listOfNotNull(
            placemark.thoroughfare,
            placemark.subThoroughfare,
            placemark.locality
        ).filter { it.isNotBlank() }

        return parts.takeIf { it.isNotEmpty() }?.joinToString(", ")
            ?: getString(Res.string.address_not_found) //"Адрес не найден"
    }
}