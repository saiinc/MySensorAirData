package com.saionji.mysensor.shared.ui.map

import com.saionji.mysensor.shared.domain.model.LatLng
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import platform.CoreLocation.CLLocationCoordinate2D

@OptIn(ExperimentalForeignApi::class)
class IosLocationService : LocationService {

    override suspend fun getCurrentLocation(callback: (LatLng?) -> Unit) {
        val result = getLocationInternal()
        callback(result)
    }

    private suspend fun getLocationInternal(): LatLng? = suspendCoroutine { continuation ->
        val locationManager = CLLocationManager()

        val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                val location = didUpdateLocations.lastOrNull() as? CLLocation
                val latLng = if (location != null) {
                    // coordinate возвращает CValue<CLLocationCoordinate2D> — wrapper для C-структуры.
                    // Используем useContents для доступа к полям структуры
                    location.coordinate.useContents { LatLng(latitude, longitude) }
                } else {
                    null
                }
                continuation.resume(latLng)
                manager.delegate = null
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                continuation.resume(null)
                manager.delegate = null
            }
        }

        locationManager.delegate = delegate
        locationManager.requestWhenInUseAuthorization()
        locationManager.requestLocation()
    }
}