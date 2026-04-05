/*
 * Copyright © Anton Sorokin 2025. All rights reserved
 */
buildscript {
    val agp_version by extra("8.2.2")
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.8.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"

    // Add the dependency for the Google services Gradle plugin
    id("com.google.gms.google-services") version "4.4.2" apply false

    // Add the dependency for the Crashlytics Gradle plugin
    id("com.google.firebase.crashlytics") version "3.0.2" apply false

    id("org.jetbrains.kotlin.multiplatform") version "2.1.0" apply false
    id("com.android.kotlin.multiplatform.library") version "8.8.0" apply false
    id("com.android.lint") version "8.8.0" apply false
    id("com.android.library") version "8.8.0" apply false
}