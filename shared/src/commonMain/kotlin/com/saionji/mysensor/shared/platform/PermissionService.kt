package com.saionji.mysensor.shared.platform

/**
 * Сервис для работы с разрешениями геолокации
 *
 * Expect/actual паттерн для кроссплатформенной работы с permissions.
 */
expect class PermissionService() {

    /**
     * Проверить, есть ли разрешения на геолокацию
     */
    fun hasLocationPermissions(context: Any?): Boolean

    /**
     * Запросить разрешения на геолокацию
     *
     * @return true если разрешения получены, false если отказано
     */
    suspend fun requestLocationPermissions(context: Any?): Boolean
}