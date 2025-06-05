/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io") // ← правильно для Kotlin DSL
    }
}

rootProject.name = "MySensor"
include(":app")
 