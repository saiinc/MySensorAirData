package com.saionji.mysensor.shared.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.saionji.mysensor.shared.domain.model.LatLng
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AndroidLocationService(
    private val context: Context
) : LocationService {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(callback: (LatLng?) -> Unit) {
        if (!hasLocationPermission()) {
            callback(null)
            return
        }

        val locationManager = context.getSystemService<LocationManager>()
        if (locationManager == null) {
            callback(null)
            return
        }

        val location = locationManager.getBestAvailableLocation()
        callback(location?.let { LatLng(it.latitude, it.longitude) })
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    private suspend fun LocationManager.getBestAvailableLocation(): Location? {
        val freshLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getCurrentLocationCompat(LocationManager.GPS_PROVIDER)
                ?: getCurrentLocationCompat(LocationManager.NETWORK_PROVIDER)
        } else {
            null
        }

        return freshLocation
            ?: getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private suspend fun LocationManager.getCurrentLocationCompat(provider: String): Location? {
        if (!isProviderEnabled(provider)) {
            return null
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            val cancellationSignal = CancellationSignal()

            continuation.invokeOnCancellation {
                cancellationSignal.cancel()
            }

            getCurrentLocation(
                provider,
                cancellationSignal,
                context.mainExecutor
            ) { location ->
                continuation.resume(location)
            }
        }
    }
}