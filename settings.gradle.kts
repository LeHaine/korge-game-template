pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven("https://plugins.gradle.org/m2/")
    }

    val korgePluginVersion: String by settings

    plugins {
        id("com.soywiz.korge") version korgePluginVersion
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace == "com.soywiz") {
                useModule("com.soywiz.korlibs.korge.plugins:korge-gradle-plugin:$korgePluginVersion")
            }
        }
    }
}

enableFeaturePreview("GRADLE_METADATA")