plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply {
    from(file("${rootDir}/gradle/artifact-settings.gradle"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

android {
    namespace = "org.maplibre.navigation.core.gms"

    defaultConfig {
        compileSdk = 35
        minSdk = 23
    }

    buildFeatures {
        buildConfig = false
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    // FLOSS navigation core
    api(project(":maplibre-navigation-core"))

    // Google Play Services location.
    // This is a regular (non-optional) dependency: this module intentionally references
    // `com.google.android.gms.*`. Apps targeting F-Droid must NOT depend on this module and
    // should use `navigation-core` directly, which stays fully FLOSS.
    api(libs.play.services.location)

    implementation(libs.kotlinx.coroutines)
}

// Exclude old version of GeoJSON libs
// At the moment a newer version - that supports Kotlin Multiplatform - is required to run navigation
configurations {
    configureEach {
        exclude(group = "org.maplibre.gl", module = "android-sdk-geojson")
        exclude(group = "org.maplibre.gl", module = "android-sdk-turf")
    }
}

apply {
    from(file("${rootDir}/gradle/publish-android.gradle"))
}
