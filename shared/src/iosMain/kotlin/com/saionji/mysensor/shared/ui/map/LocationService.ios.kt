package com.saionji.mysensor.shared.ui.map

import com.saionji.mysensor.shared.domain.model.LatLng
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import platform.CoreLocation.CLLocationCoordinate2D

@OptIn(ExperimentalForeignApi::class)
class IosLocationService : LocationService {

    private var activeLocationManager: CLLocationManager? = null
    private var activeDelegate: CLLocationManagerDelegateProtocol? = null

    override suspend fun getCurrentLocation(callback: (LatLng?) -> Unit) {
        val result = getLocationInternal()
        callback(result)
    }

    private suspend fun getLocationInternal(): LatLng? = suspendCancellableCoroutine { continuation ->
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
                if (continuation.isActive) {
                    continuation.resume(latLng)
                }
                manager.stopUpdatingLocation()
                clearActiveRequest(manager)
            }

            override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
                manager.stopUpdatingLocation()
                clearActiveRequest(manager)
            }
        }

        activeLocationManager = locationManager
        activeDelegate = delegate
        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.startUpdatingLocation()

        continuation.invokeOnCancellation {
            locationManager.stopUpdatingLocation()
            clearActiveRequest(locationManager)
        }
    }

    private fun clearActiveRequest(manager: CLLocationManager) {
        manager.delegate = null
        activeDelegate = null
        activeLocationManager = null
    }
}
