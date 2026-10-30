plugins {
    id("java-library")
    id("maven-publish")
    id("all")
}

repositories {
    maven("https://nexus.modl.gg/repository/maven-releases/")
    maven("https://nexus.modl.gg/repository/maven-snapshots/")
    mavenCentral()
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://jitpack.io")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.alessiodp.com/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://maven.chamosmp.net/releases")
    maven("https://repo.faststats.dev/releases")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
    options.compilerArgs.add("-Xlint:-options")
    options.compilerArgs.add("-Xlint:deprecation")
}