/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

package com.saionji.mysensor.shared.data.repository

import android.location.Geocoder
import com.saionji.mysensor.shared.domain.model.GeocodingRepository
import com.saionji.mysensor.shared.generated.resources.Res
import com.saionji.mysensor.shared.generated.resources.address_not_found
import com.saionji.mysensor.shared.generated.resources.geocoding_error
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString

/**
 * Android-реализация репозитория геокодинга
 * 
 * Использует Android Geocoder API для получения адреса по координатам
 */
class AndroidGeocodingRepository(
    private val geocoder: Geocoder
) : GeocodingRepository {

    override suspend fun getAddress(
        lat: Double?,
        lon: Double?
    ): String = withContext(Dispatchers.IO) {

        if (lat != null && lon != null) {
            try {
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                addresses?.firstOrNull()?.let { addr ->
                    listOfNotNull(
                        addr.thoroughfare,
                        addr.subThoroughfare,
                        addr.locality
                    ).joinToString(", ")
                } ?: getString(Res.string.address_not_found) //"Адрес не найден"
            } catch (_: Exception) {
                getString(Res.string.geocoding_error) //"Ошибка геокодинга"
            }
        }
        else {
            return@withContext ""
        }
    }
}