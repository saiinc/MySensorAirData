# My Sensor

[![CI](https://github.com/saiinc/MySensorAirData/workflows/CI/badge.svg)](https://github.com/saiinc/MySensorAirData/actions)
[![Android](https://github.com/saiinc/MySensorAirData/workflows/Android/badge.svg)](https://github.com/saiinc/MySensorAirData/actions)
[![iOS Status](https://github.com/saiinc/MySensorAirData/workflows/iOS/badge.svg)](https://github.com/saiinc/MySensorAirData/actions)

**My Sensor** is an Android and iOS application for air quality monitoring. It retrieves PM2.5, PM10, temperature, humidity, and pressure data from [sensor.community](https://sensor.community/) and displays it on your smartphone. The iOS version is ready, but not yet published.

**My Sensor** — это Android и iOS приложение для мониторинга качества воздуха. Оно получает данные о PM2.5, PM10, температуре, влажности и давлении с платформы [sensor.community](https://sensor.community/) и отображает их на вашем смартфоне. iOS-версия готова, но ещё не опубликована.

## 📥 Installation / Установка

1. **Download the latest version** from the [Releases page](https://github.com/saiinc/MySensorAirData/releases).  
2. **Install the APK file** on your Android device.  
3. Grant the necessary permissions when you first launch the app. 
-
1. **Скачайте последнюю версию** со [страницы релизов](https://github.com/saiinc/MySensorAirData/releases).  
2. **Установите APK-файл** на ваше Android-устройство.  
3. При первом запуске предоставьте необходимые разрешения для работы приложения.    

## 📍 Features / Возможности

### 📊 Local Air Quality Dashboard / Сводка общего состояния качества воздуха
You can use the app as a **dashboard** to monitor air quality in your area.  
Вы можете использовать приложение как **дашборд** для мониторинга качества воздуха в вашем регионе.   
<img width="502" height="893" alt="dashboard_mysensor" src="https://github.com/user-attachments/assets/252986df-96e6-4298-9440-9454bb5c0317" />

### 🏠 Personal Sensor Monitoring / Мониторинг персонального сенсора
The app allows you to monitor **a specific sensor** in real-time.  
Приложение позволяет отслеживать данные **конкретного сенсора** в режиме реального времени.  
<img width="502" height="891" alt="home_mysensor" src="https://github.com/user-attachments/assets/9d923100-cb26-4b2e-a5f9-fd43b0600343" />

---

<a href="https://f-droid.org/packages/com.saionji.mysensor/">
    <img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
         alt="Get it on F-Droid"
         height="80">
</a>

## Android Build Variants

The Android app has two store flavors:

- `play` - regular build with Firebase Analytics and Firebase Crashlytics.
- `fdroid` - F-Droid-oriented build without Firebase, Crashlytics, or Google Play Services Location.

The `fdroid` variant can run without `local.properties` and without a MapTiler API key by using the built-in raster map style. It does not send crash reports automatically; if a crash report is created, the user can explicitly share it through Android's standard share/email UI.

Useful commands:

```powershell
.\gradlew.bat :app:assemblePlayDebug
.\gradlew.bat :app:assembleFdroidDebug
.\gradlew.bat :app:assembleFdroidRelease
```
