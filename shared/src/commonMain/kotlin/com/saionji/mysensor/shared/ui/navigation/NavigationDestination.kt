package com.saionji.mysensor.shared.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Роуты навигации для Multiplatform Navigation Compose
 *
 * Каждый роут — это @Serializable объект.
 * Типобезопасная навигация без строковых ключей.
 */

/**
 * Главный экран (Dashboard + Map)
 */
@Serializable
object Main

/**
 * Экран "О приложении"
 */
@Serializable
object About
