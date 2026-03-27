package com.saionji.mysensor.shared.ui.navigation

sealed class NavigationDestination {
    object About : NavigationDestination()

    fun toRoute(): String = when (this) {
        About -> "about"
    }
}