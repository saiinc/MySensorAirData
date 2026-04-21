package com.saionji.mysensor.shared.platform

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class PermissionService {

    actual fun hasLocationPermissions(context: Any?): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == 3 || status == 4
    }

    actual suspend fun requestLocationPermissions(context: Any?): Boolean {
        if (hasLocationPermissions(context)) return true

        return suspendCancellableCoroutine { continuation ->
            val locationManager = CLLocationManager()

            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    val granted = hasLocationPermissions(null)
                    continuation.resume(granted)
                    manager.delegate = null
                }
            }

            locationManager.delegate = delegate
            locationManager.requestWhenInUseAuthorization()
        }
    }
}