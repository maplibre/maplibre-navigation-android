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
    namespace = "org.maplibre.navigation.location.gms"

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
    implementation(project(":maplibre-navigation-core"))
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines)
}

apply {
    from(file("${rootDir}/gradle/publish-android.gradle"))
}
