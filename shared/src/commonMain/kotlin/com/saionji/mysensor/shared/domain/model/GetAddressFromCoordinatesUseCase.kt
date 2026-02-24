package com.saionji.mysensor.shared.domain.model

interface GeocodingRepository {
    suspend fun getAddress(lat: Double?, lon: Double?): String
}

class GetAddressFromCoordinatesUseCase(
    private val repository: GeocodingRepository
) {
    suspend operator fun invoke(
        lat: Double?,
        lon: Double?
    ): String {
        return repository.getAddress(lat, lon)
    }
}