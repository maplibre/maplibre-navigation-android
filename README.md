<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://maplibre.org/img/maplibre-logos/maplibre-logo-for-dark-bg.svg">
    <source media="(prefers-color-scheme: light)" srcset="https://maplibre.org/img/maplibre-logos/maplibre-logo-for-light-bg.svg">
    <img alt="MapLibre Logo" src="https://maplibre.org/img/maplibre-logos/maplibre-logo-for-light-bg.svg" width="200">
  </picture>
</p>

MapLibre Navigation SDK for Android (and KMP)
=============================================

The Maplibre Navigation SDK for Android is built on a fork of the [Mapbox Navigation SDK v0.19](https://github.com/flitsmeister/flitsmeister-navigation-android/tree/v0.19.0) which is built on top of the [Mapbox Directions API](https://www.mapbox.com/directions) and contains the logic needed to get timed navigation instructions.

With this SDK you can implement turn-by-turn navigation in your own Android app while hosting your Map tiles and Directions API.

> [!NOTE]  
> We are currently active working on converting this pure Android library to a Kotlin Multiplatform library.

<div align="center">
  <img src="https://github.com/maplibre/maplibre-navigation-android/blob/main/.github/preview.png" height="350px" alt="MapLibre Navigation Android">
</div>

## License

- 100% Open Source
- [MIT License](LICENSE)
- No Telemetry


## Why have we forked

1. Mapbox decided to put a closed-source component to their navigation SDK and introduced a non-open-source license. Maplibre wants an open-source solution.
2. Mapbox decided to put telemetry in their SDK. We couldn't turn this off without adjusting the source.
3. We want to use the SDK without paying Mapbox for each MAU and without Mapbox API keys.

All issues are covered with this SDK. 

## What have we changed

- We completely removed the UI part from the SDK so it will only contain the logic for navigation and not the visuals.
- We upgraded the [Mapbox Maps SDK for Android](https://github.com/mapbox/mapbox-gl-native/tree/master/platform/android) to [MapLibre Native for Android](https://github.com/maplibre/maplibre-gl-native/tree/master/platform/android) version 9.4.0.
- We upgraded the [NavigationRoute](https://github.com/flitsmeister/flitsmeister-navigation-android/blob/master/libandroid-navigation/src/main/java/com/mapbox/services/android/navigation/v5/navigation/NavigationRoute.java#L425) 
 with the possibility to add an interceptor to the request.
- We changed the [locationLayerPlugin](https://github.com/mapbox/mapbox-plugins-android) to the [location component](https://docs.mapbox.com/android/api/map-sdk/8.5.0/com/mapbox/mapboxsdk/location/LocationComponent.html)
- We updated the logic around the implementation of the locationEngine so it can be used with the new locationEngine from the [Mapbox SDK](https://github.com/mapbox/mapbox-gl-native/tree/master/platform/android).
- We removed the telemetry class from the project. Nothing is being sent to Mapbox or Maplibre.

## Getting Started

This library is available on Maven Central. To use it, add the following to your `build.gradle`:

### Gradle

Step 2. Add the dependency
```groovy
  // Core KMP library (without UI)
  implementation 'org.maplibre.navigation:navigation-core:5.0.0-pre10'

  // UI library (Android only)
  implementation 'org.maplibre.navigation:navigation-ui-android:5.0.0-pre10'
```


## Getting Help

- **Have a bug to report?** [Open an issue](https://github.com/maplibre/maplibre-navigation-android/issues). If possible, include the version of MapLibre Services, a full log, and a project that shows the issue.
- **Have a feature request?** [Open an issue](https://github.com/maplibre/maplibre-navigation-android/issues/new). Tell us what the feature should do and why you want the feature.

### Samples

#### Android

We have added [an Android example app](https://github.com/maplibre/maplibre-navigation-android/tree/main/app/src/main/java/org/maplibre/navigation/android/example) to this repo.

The Valhalla example is aready pre-configured. All other examples needs to be configured with your own tile and direction server. This URLs needs to be set in `developer-config.xml`, which is generated on first Gradle run. 

#### Multiplatform

The new Kotlin Multiplatform library for iOS and Android has it's own small [sample apps](https://github.com/maplibre/maplibre-navigation-android/tree/main/sample) for all supported platforms.

### Contributing

We welcome feedback, translations, and code contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details.




