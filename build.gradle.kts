plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.cocoapods) apply false
    alias(libs.plugins.kotlin.dokka) apply false
    alias(libs.plugins.kotlinx.serialization) apply false
    alias(libs.plugins.nexus)
}

apply {
    from(file("${rootDir}/gradle/artifact-settings.gradle"))
    from(file("${rootDir}/gradle/publish-root.gradle"))
}

nexusPublishing {
    repositories {
        sonatype {
            useStaging.set((project.properties.get("isSnapshot") as Boolean).not())
            stagingProfileId.set(project.extra["sonatypeStagingProfileId"] as String?)

            username.set(project.extra["ossrhUsername"] as String?)
            password.set(project.extra["ossrhPassword"] as String?)

            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }

    transitionCheckOptions {
        maxRetries.set(120)
        delayBetween.set(java.time.Duration.ofSeconds(10))
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
