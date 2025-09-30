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
    namespace = "org.maplibre.navigation.android.navigation.ui.v5"

    defaultConfig {
        compileSdk = 35
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables.useSupportLibrary = true

        buildConfigField(
            "String",
            "MAPLIBRE_NAVIGATION_SDK_IDENTIFIER",
            String.format("\"%s\"", "maplibre-navigation-ui-android")
        )
        buildConfigField(
            "String",
            "MAPLIBRE_NAVIGATION_EVENTS_USER_AGENT", String.format(
                "\"maplibre-navigation-ui-android/%s\"",
                project.extra["versionName"]
            )
        )

        consumerProguardFiles("proguard-consumer.pro")
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    configurations.create("javadocDeps")

    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    // Navigation SDK
    api(project(":maplibre-navigation-core"))

    api(libs.maplibre.annotation)
    implementation(libs.maplibre)

    // Mapbox SDKs (needed for requests)
    api(libs.mapbox.geojson)

    // Support libraries
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)

    // AutoValues
    annotationProcessor(libs.autovalue)
    implementation(libs.autovalue.annotations)

    // Picasso
    implementation(libs.picasso)

    // Timber
    implementation(libs.timber)

    // Unit testing
    testImplementation(libs.junit)
    testImplementation(libs.mockito)
    testImplementation(libs.robolectric)
    testImplementation(libs.json)
    testImplementation(libs.mockk)
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
    from(file("javadoc.gradle"))
    from(file("${rootDir}/gradle/publish-android.gradle"))
}
