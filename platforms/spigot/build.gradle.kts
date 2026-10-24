plugins {
    id("sub")
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.polar.top/repository/polar/")
    maven("https://repo.janmm14.de/repository/intave/")
    maven("https://eldonexus.de/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)

    annotationProcessor(libs.lombok)

    // Anticheat APIs
    compileOnly(libs.polar.api)
    compileOnly(libs.intave.access)

    // Internal modules
    compileOnly(project(":bridge-core"))

    // Command framework
    compileOnly(libs.lamp.common)
    compileOnly(libs.lamp.bukkit)
    compileOnly(libs.lamp.brigadier)

    // I'm too lazy to learn lamp
    compileOnly(libs.strokk.paper)
    annotationProcessor(libs.strokk.paper.processor)

    // Cirrus menu system
    compileOnly(libs.cirrus.api)
    compileOnly(libs.cirrus.spigot)

    // PacketEvents
    compileOnly(libs.packetevents.spigot)

    // Replay recording
    implementation(libs.replay.recording)

    testImplementation(project(":bridge-core"))
    testImplementation(project(":core"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.mockito.junit.jupiter)
    testImplementation(libs.lamp.common)
    testImplementation(libs.lamp.bukkit)
    testImplementation(libs.cirrus.api)
    testImplementation(libs.cirrus.spigot)
    testImplementation(libs.paper.api)
    testImplementation(libs.packetevents.spigot)
}

tasks.processResources {
    val props = mapOf("version" to version, "description" to description)
    inputs.properties(props)
    filteringCharset = "UTF-8"

    filesMatching("paper-plugin.yml") {
        expand(props)
    }
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "gg.modl.minecraft.platform.spigot.plugin")
    }
}

tasks.test {
    useJUnitPlatform()
}