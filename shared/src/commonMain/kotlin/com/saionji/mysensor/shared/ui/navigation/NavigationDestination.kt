package com.saionji.mysensor.shared.ui.navigation

/**
 * Роуты навигации для приложения
 * 
 * Используется sealed class для типобезопасности.
 * Совместим с AndroidX Navigation (строковые роуты).
 */
sealed class NavigationDestination {
    
    /**
     * Экран "О приложении"
     */
    object About : NavigationDestination()

    /**
     * Преобразование в строковый роут для AndroidX Navigation
     */
    fun toRoute(): String = when (this) {
        About -> "about"
    }
}
