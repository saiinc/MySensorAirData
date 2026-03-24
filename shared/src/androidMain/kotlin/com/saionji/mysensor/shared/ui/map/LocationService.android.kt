package com.saionji.mysensor.shared.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.saionji.mysensor.shared.domain.model.LatLng

class AndroidLocationService(
    private val context: Context
) : LocationService {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(callback: (LatLng?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).addOnSuccessListener { location ->
            if (location != null) {
                callback(LatLng(location.latitude, location.longitude))
            } else {
                callback(null)
            }
        }.addOnFailureListener {
            Log.e("Location", "Ошибка при получении текущего местоположения", it)
            callback(null)
        }
    }
}