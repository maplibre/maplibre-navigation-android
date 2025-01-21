pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
//        // KMP dependencies currently only build locally
//        mavenLocal()
        maven { url = uri("https://mvn.fabiankeunecke.de") }
    }
}

rootProject.name = "ML Nav - Android Sample App"
include(":app")
 