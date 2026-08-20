pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // TODO: WASM/JS Multiplatform builds are adding own repositories on project level.
    //  Currently the only option is to allow this, until the KMP framework deliver us other options.
    //  See: https://youtrack.jetbrains.com/projects/KT/issues/KT-68533/Kotlin-2.0-WasmJs-error-when-using-RepositoriesMode.FAILONPROJECTREPOS
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "maplibre-navigation"

include(":maplibre-navigation-core")
include(":maplibre-navigation-location-gms")
include(":libandroid-navigation-ui")
include(":app")