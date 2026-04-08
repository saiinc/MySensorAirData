package com.saionji.mysensor.shared.platform

import platform.CoreLocation.CLLocationManager

actual class PermissionService {

    actual fun hasLocationPermissions(context: Any?): Boolean {
        val status = CLLocationManager.authorizationStatus()
        // CLAuthorizationStatus числовые значения:
        // 0 = NotDetermined, 1 = Restricted, 2 = Denied
        // 3 = AuthorizedWhenInUse, 4 = AuthorizedAlways
        val statusValue = status
        return statusValue == 3 || statusValue == 4

    }

    actual suspend fun requestLocationPermissions(context: Any?): Boolean {
        // На iOS запрос делается через CLLocationManager.requestWhenInUseAuthorization()
        // который вызывается в LocationService
        return hasLocationPermissions(context)
    }
}