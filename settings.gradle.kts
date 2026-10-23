pluginManagement {
    val rootGradleProperties = java.util.Properties().apply {
        file("gradle.properties").reader().use { reader ->
            load(reader)
        }
    }
    val shadowPluginVersion = rootGradleProperties.getProperty("shadow.version")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.gradleup.shadow") version (shadowPluginVersion ?: throw GradleException("shadow.version missing from gradle.properties"))
    }
}

rootProject.name = "modl-minecraft"

include("api")
include("core")
include("bridge-core")
include("platforms:spigot")
include("platforms:spigot-sv")
include("platforms:velocity")
include("distribution")
