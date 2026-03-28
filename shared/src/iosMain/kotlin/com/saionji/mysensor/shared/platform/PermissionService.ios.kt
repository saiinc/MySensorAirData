package com.saionji.mysensor.shared.platform


/**
 * iOS реализация PermissionService
 *
 * Использует CoreLocation для работы с разрешениями геолокации.
 */
actual class PermissionService {

    actual fun hasLocationPermissions(context: Any?): Boolean {
        // TODO: Реализовать с CoreLocation
        return true // Заглушка
    }

    actual suspend fun requestLocationPermissions(context: Any?): Boolean {
        // TODO: Реализовать с CoreLocation
        return true // Заглушка
    }
}