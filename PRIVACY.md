# Privacy Policy

My Sensor is an air quality monitoring app. It displays sensor data such as PM2.5, PM10, temperature, humidity, and pressure from public air quality data sources.

This document describes the current data handling behavior of the app.

## Build Variants

The Android app may be distributed in different build variants.

### Play / Direct APK Variant

The `play` variant may include:

- Firebase Analytics for basic app usage analytics.
- Firebase Crashlytics for crash reports and diagnostics.
- MapTiler map styles when a MapTiler API key is configured.

### F-Droid Variant

The `fdroid` variant is built without:

- Firebase Analytics.
- Firebase Crashlytics.
- Google Play Services Location.

The `fdroid` variant uses Android framework location APIs. It can run without `local.properties` and without a MapTiler API key by using the built-in raster map style.

The `fdroid` variant does not send crash reports automatically. If a crash report is created, it is stored locally on the device and can be shared only after explicit user action through Android's standard share/email flow.

## Data Sources

The app requests air quality sensor data from [sensor.community](https://sensor.community/).

When the app loads sensor data for an area or a selected sensor, it sends the required request parameters to the sensor data API, such as sensor identifiers or map area coordinates.

## Location

The app may request access to your device location so it can show nearby air quality data and position the map around your area.

Location access is used for app functionality. The app does not intentionally store your precise location on a custom backend operated by this project.

The `fdroid` variant uses Android framework location APIs and does not include Google Play Services Location.

You can deny or revoke location permission in your device settings. Some map and nearby-sensor features may be limited without location access.

## App Settings

The app may store local preferences on your device, such as selected sensors or display settings. These settings are stored locally and are used to restore your app experience.

## Third-Party Services

The app may use third-party services and libraries, including:

- sensor.community for public air quality sensor data.
- CARTO basemaps / `basemaps.cartocdn.com` for fallback map tiles.
- MapLibre for map display.
- MapTiler / `api.maptiler.com` when a MapTiler style URL is configured.
- Firebase Analytics in the `play` variant only.
- Firebase Crashlytics in the `play` variant only.

These third-party services may process technical data such as device information, crash logs, app version, usage events, IP address, or map/API request data according to their own privacy policies.

The `fdroid` variant does not include Firebase Analytics, Firebase Crashlytics, or Google Play Services Location.

## Data Sharing

This project does not sell personal data.

Data may be processed by third-party services used by the app to provide maps, location features, analytics, crash diagnostics, and public sensor data.

In the `fdroid` variant, crash reports are not shared automatically. They are shared only if the user explicitly chooses to send them through Android's standard share/email UI.

## Children's Privacy

The app is not designed to knowingly collect personal information from children.

## Changes

This privacy policy may be updated when app behavior, dependencies, or data handling changes. Updates should be committed to this repository together with the relevant app changes.

## Contact

For privacy questions, contact the maintainer through the GitHub profile linked from this repository.
