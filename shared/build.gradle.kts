import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    id("org.jetbrains.compose") version "1.9.3"
}

android {
    namespace = "com.saionji.mysensor.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets

    
    // Apply Compose compiler plugin to all targets
    applyDefaultHierarchyTemplate()
    
    // Configure Compose for all targets
    compilerOptions {
        freeCompilerArgs.addAll("-Xexpect-actual-classes")
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcf = XCFramework()

    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            xcf.add(this)
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")
                // Add KMP dependencies here
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                // Ktor для KMP
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                implementation("com.russhwolf:multiplatform-settings:1.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.0")
                implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.9.6")
                // Compose Multiplatform
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)
                // Multiplatform Navigation Compose
                implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
                implementation("app.cash.turbine:turbine:1.0.0")
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
                implementation("app.cash.turbine:turbine:1.0.0")
            }
        }

        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
                implementation("io.ktor:ktor-client-okhttp:2.3.7")
                implementation("androidx.datastore:datastore-preferences:1.1.1")
                // Android Compose dependencies
                implementation(compose.uiTooling)
                implementation(compose.preview)
                implementation("androidx.activity:activity-compose:1.9.0")
                implementation("com.google.android.gms:play-services-location:21.3.0")
                implementation("org.maplibre.gl:android-sdk:11.8.0")
            }
        }

//        getByName("androidDeviceTest") {
//            dependencies {
//                implementation("androidx.test:runner:1.5.2")
//                implementation("androidx.test:core:1.5.0")
//                implementation("androidx.test.ext:junit:1.3.0")
//            }
//        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
                implementation("io.ktor:ktor-client-darwin:2.3.7")
                // iOS Compose dependencies
            }
        }
    }

}

compose {
    resources {
        publicResClass = true
        packageOfResClass = "com.saionji.mysensor.shared.generated.resources"
    }
}