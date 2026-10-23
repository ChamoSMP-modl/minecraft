import java.util.zip.ZipFile
import java.util.Properties

plugins {
    id("com.gradleup.shadow")
}

java {
    disableAutoTargetJvm()
}

val rootGradlew = rootProject.file("gradlew")
val packetEventsDir = rootProject.file("../minecraft-packetevents")
val packetEventsGradlew = packetEventsDir.resolve("gradlew")
val nestedPacketEventsFileName = "packetevents-modl.jar"


fun packetEventsVersion(): String {
    val properties = Properties()
    packetEventsDir.resolve("gradle.properties").inputStream().use(properties::load)
    val fullVersion = properties.getProperty("fullVersion")
    val snapshot = properties.getProperty("snapshot").toBoolean()
    return if (snapshot) "$fullVersion-SNAPSHOT" else fullVersion
}

dependencies {
    implementation(project(":core"))
    implementation(project(":bridge-core"))
    implementation(project(":platforms:spigot"))
    implementation(project(":platforms:spigot-sv"))
    implementation(project(":platforms:velocity"))
    implementation(libs.adventure.api)
    implementation(libs.adventure.nbt)
    implementation(libs.adventure.minimessage)
    implementation(libs.adventure.serializer.legacy)
    implementation(libs.adventure.serializer.gson)
}

tasks.shadowJar {
    dependsOn(":platforms:velocity:check")

    archiveBaseName.set("modl")
    archiveClassifier.set("")

    relocate("com.google.protobuf", "gg.modl.libs.protobuf")

    exclude("**/module-info.class")
    exclude("com/google/gson/**")

    // Dependencies loaded at runtime via Libby (Libraries.PROTO_DEPS)
    exclude("gg/modl/proto/**")
    exclude("com/google/protobuf/**")
    exclude("com/google/common/**")
    exclude("com/google/thirdparty/**")
    exclude("com/google/errorprone/**")
    exclude("com/google/j2objc/**")
    exclude("com/google/code/**")
    exclude("google/protobuf/**")
    exclude("org/projectnessie/**")
    exclude("org/agrona/**")
    exclude("com/buf/**")
    exclude("build/buf/**")
    exclude("inet/ipaddr/**")
    exclude("org/checkerframework/**")
    exclude("jakarta/**")
    exclude("javax/annotation/**")

    from(rootProject.file("LICENSE.txt"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}

val universalDistributionJar = layout.buildDirectory.file("libs/modl-${project.version}.jar")

fun zipEntryNames(file: File): Set<String> =
    ZipFile(file).use { zip -> zip.entries().asSequence().map { it.name }.toSet() }
