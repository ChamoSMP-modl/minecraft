plugins {
    id("sub")
}

dependencies {
    compileOnly(libs.velocity.api)
    annotationProcessor(libs.velocity.api)
    annotationProcessor(libs.lombok)

    implementation(libs.faststats.velocity)

    compileOnly(project(":core"))

    compileOnly(libs.lamp.common)
    compileOnly(libs.lamp.brigadier)
    compileOnly(libs.lamp.velocity)

    implementation(libs.cirrus.velocity)

    implementation(libs.libby.velocity)

    compileOnly(libs.packetevents.velocity)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(project(":core"))
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "gg.modl.minecraft.platform.velocity.plugin")
    }
}

tasks.test {
    useJUnitPlatform()
}