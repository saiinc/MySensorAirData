package com.saionji.mysensor.shared.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android реализация PermissionService
 *
 * Использует ContextCompat для проверки разрешений.
 */
actual class PermissionService {

    actual fun hasLocationPermissions(context: Any?): Boolean {
        val ctx = context as? Context ?: return false

        val coarsePermission = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val finePermission = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        return coarsePermission == PackageManager.PERMISSION_GRANTED ||
                finePermission == PackageManager.PERMISSION_GRANTED
    }

    actual suspend fun requestLocationPermissions(context: Any?): Boolean {
        // Для запроса разрешений нужен Activity, а не просто Context
        // Поэтому этот метод только проверяет текущий статус
        // Реальный запрос делается через Accompanist в Composable

        return hasLocationPermissions(context)
    }
}