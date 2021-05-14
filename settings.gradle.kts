pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm").version(kotlinVersion)
    }
}

rootProject.name = "korge-plugins-root"

//include(":korge-jvm-bundle")
//include(":korge-build")
include(":korge-gradle-plugin")
//include(":korge-gradle-plugin-android")
