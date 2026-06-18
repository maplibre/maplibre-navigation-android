pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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