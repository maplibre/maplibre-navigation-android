plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

apply {
    from(file("${rootDir}/gradle/artifact-settings.gradle"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain(21)
}

android {
    namespace = "org.maplibre.navigation.location.gms"

    defaultConfig {
        compileSdk = 37
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
    implementation(project(":maplibre-navigation-core"))
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines)
}

apply {
    from(file("${rootDir}/gradle/publish-android.gradle"))
}
