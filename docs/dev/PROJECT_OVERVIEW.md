# My Sensor Developer Overview

This document summarizes the current technical state of My Sensor for maintainers and contributors.

My Sensor is an air quality monitoring app that reads public sensor data from sensor.community and displays PM2.5, PM10, temperature, humidity, and pressure values in a dashboard and on a map.

## Project Status

- Android is the primary production target.
- Kotlin Multiplatform migration is in progress and already active through the `shared` module.
- iOS support exists through `iosApp` and a shared Compose UI entry point, but should be treated as under active development.
- Map support is intentionally platform-specific: shared code owns state and business logic, while Android and iOS provide native MapLibre integrations.

## Modules

| Module | Purpose |
| --- | --- |
| `app` | Android application shell, Android-specific map view, Firebase setup, permissions entry points, release configuration. |
| `shared` | Kotlin Multiplatform module with domain, data, networking, ViewModels, shared Compose UI, resources, and platform abstractions. |
| `iosApp` | iOS host app that embeds the shared Compose UI through `MainViewController`. |

## Technology Stack

### Core

- Kotlin `2.2.20`
- Android Gradle Plugin `8.8.0`
- Java/JVM target `17`
- Gradle Kotlin DSL
- Kotlin Multiplatform
- Compose Multiplatform `1.9.3`
- Jetpack Compose / Material 3

### Networking and Data

- Ktor client `2.3.7`
- Kotlinx Serialization JSON `1.6.3`
- sensor.community API: `https://data.sensor.community/airrohr/v1/`
- Multiplatform Settings `1.1.1`
- Android DataStore is still present for migration from older local settings.

### Maps and Location

- MapLibre Android SDK `11.8.0`
- MapLibre iOS wrapper code in `iosApp` and `shared/src/iosMain`
- MapTiler style configuration through local build settings
- Google Play Services Location on Android
- Core Location on iOS

### Diagnostics

- Firebase Analytics
- Firebase Crashlytics

### Testing

- Kotlin test
- JUnit 4
- kotlinx-coroutines-test
- Turbine
- Fake repositories for shared ViewModel tests
- GitHub Actions for unit tests, Android debug APK builds, and iOS simulator builds

## Architecture

The project follows a practical layered architecture:

- UI: Compose screens, shared where possible.
- Presentation: shared ViewModels expose state through flows and Compose state.
- Domain: use cases transform and coordinate sensor data.
- Data: repositories adapt API responses and local settings.
- Network: Ktor service calls sensor.community endpoints.
- Platform layer: Android and iOS implement location, geocoding, sharing, HTTP engines, themes, and map integration.

The main architectural rule is that `commonMain` must not depend on Android-only or iOS-only map SDKs. It can define map state, models, ViewModels, and controller interfaces, but native map widgets stay in platform source sets or app shells.

## Shared Module Layout

Important `shared/src/commonMain` areas:

- `data/model`: app settings and dashboard models.
- `data/repository`: settings and network repository implementations.
- `domain/model`: domain map/location models and geocoding contract.
- `domain/repository`: repository contracts.
- `domain/usecase`: sensor loading use cases.
- `network/model`: serialized sensor.community response models.
- `network/service`: Ktor service and `HttpClientFactory` expect declaration.
- `ui/app`: shared app content and navigation host.
- `ui/screens`: dashboard, home, share, about, and main screen content.
- `ui/map`: shared map state, ViewModel, controller contract, popup UI, and map screen content.
- `ui/components`: shared app bar, settings dialog, error banner, and platform feature flags.
- `ui/theme`: shared theme plus platform hooks.
- `composeResources`: localized strings and shared drawable resources.

Platform-specific source sets:

- `shared/src/androidMain`: Android HTTP engine, geocoding, location service, permissions, map controller, theme/status bar, share manager, and DataStore migration.
- `shared/src/iosMain`: iOS HTTP engine, geocoding, location service, permissions, MapLibre bridge/controller, theme hooks, share manager, and iOS container.

## App Flow

1. The Android app starts in `MainActivity`.
2. `MySensorApplication` initializes the Android container and runs local settings migration.
3. `AndroidAppContainer` delegates shared dependencies to `SharedContainerImpl`.
4. `SharedContainerImpl` creates the Ktor sensor service, repositories, and use cases.
5. `SensorsAppContent` renders the shared Compose application shell.
6. Dashboard data is loaded through `MySensorViewModel` and `GetSensorValuesUseCase`.
7. Map data is loaded through `SharedMapViewModel` and `GetSensorValuesByAreaUseCase`.

The iOS app embeds `MainViewController()` from the shared module into SwiftUI through `ContentView`.

## Map Design

MapLibre is cross-platform, but not KMP-native in this project. Android and iOS use different native integrations.

Shared code owns:

- `SharedMapViewModel`
- `MapUiState`
- `MapBounds`
- `MapMarker`
- `MapSensor`
- `MapMeasurement`
- `MapController` contract
- common map UI pieces such as marker popup and value controls

Platform code owns:

- Android `MapLibreView`
- Android marker rendering
- Android `AndroidMapLibreController`
- iOS `IosMapView`
- iOS `IosMapController`
- iOS `MapLibreWrapper`

This keeps business logic reusable while allowing each platform to use its native MapLibre SDK.

## Configuration

Local/private files must not be committed.

Android local configuration:

- `local.properties`
- `MAPTILER_API_KEY`
- `MAPTILER_STYLE_ID`, default `streets-v4`
- `MAPTILER_USER_AGENT`, default `com.saionji.mysensor`
- `app/google-services.json` for Firebase

iOS local configuration:

- `iosApp/Configuration/Local.xcconfig`
- `iosApp/Configuration/Local.xcconfig.example` is safe to commit as a template
- `iosApp/Configuration/Config.xcconfig` contains non-secret defaults

Signing keys, generated APKs/AABs, Firebase JSON files, and local release artifacts should stay ignored by `.gitignore`.

## Build and Test Commands

Android debug APK:

```bash
./gradlew :app:assembleDebug
```

Android unit tests:

```bash
./gradlew testDebugUnitTest
```

Shared Android tests:

```bash
./gradlew :shared:testDebugUnitTest
```

Shared iOS XCFramework:

```bash
./gradlew :shared:assembleSharedDebugXCFramework
```

The iOS app requires macOS and Xcode for simulator/device builds.

## GitHub Actions

The repository has three workflows:

- `CI`: runs unit tests, then builds and uploads a debug APK artifact.
- `Android`: runs after successful CI and builds the Android debug APK.
- `iOS`: runs after successful CI on macOS, builds the shared iOS XCFramework, then builds the iOS simulator app.

The CI workflow expects `GOOGLE_SERVICES_JSON` as a base64-encoded repository secret.

## Current Development Guidelines

- Keep cross-platform business logic in `shared/src/commonMain`.
- Put Android-only code in `app` or `shared/src/androidMain`.
- Put iOS-only code in `iosApp` or `shared/src/iosMain`.
- Do not import Android SDK, iOS SDK, MapLibre Android, or UIKit directly from `commonMain`.
- Prefer small platform abstractions over duplicating business logic.
- Keep API keys, Firebase files, signing keys, generated APKs, and local IDE files out of Git.
- Update this document when the architecture or build flow changes.
