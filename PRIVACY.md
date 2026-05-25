# Privacy Policy

My Sensor is an air quality monitoring app. It displays sensor data such as PM2.5, PM10, temperature, humidity, and pressure from public air quality data sources.

This document describes the current data handling behavior of the app.

## Data Sources

The app requests air quality sensor data from [sensor.community](https://sensor.community/).

When the app loads sensor data for an area or a selected sensor, it sends the required request parameters to the sensor data API, such as sensor identifiers or map area coordinates.

## Location

The app may request access to your device location so it can show nearby air quality data and position the map around your area.

Location access is used for app functionality. The app does not intentionally store your precise location on a custom backend operated by this project.

You can deny or revoke location permission in your device settings. Some map and nearby-sensor features may be limited without location access.

## App Settings

The app may store local preferences on your device, such as selected sensors or display settings. These settings are stored locally and are used to restore your app experience.

## Third-Party Services

The app may use third-party services and libraries, including:

- sensor.community for public air quality sensor data.
- MapTiler and MapLibre-related map services for map display.
- Google Play services for Android location features.
- Firebase Analytics for basic app usage analytics.
- Firebase Crashlytics for crash reports and diagnostics.

These third-party services may process technical data such as device information, crash logs, app version, usage events, IP address, or map/API request data according to their own privacy policies.

## Data Sharing

This project does not sell personal data.

Data may be processed by third-party services used by the app to provide maps, location features, analytics, crash diagnostics, and public sensor data.

## Children's Privacy

The app is not designed to knowingly collect personal information from children.

## Changes

This privacy policy may be updated when app behavior, dependencies, or data handling changes. Updates should be committed to this repository together with the relevant app changes.

## Contact

For privacy questions, contact the maintainer through the GitHub profile linked from this repository.
