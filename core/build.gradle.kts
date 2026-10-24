plugins {
    id("sub")
}

buildscript {
    // Version is read straight from the [versions] table of the catalog so it can never drift from libs.snakeyaml.
    val versionsToml = rootDir.resolve("gradle/libs.versions.toml").readText()
    val snakeYamlVersion = Regex("""^snakeyaml\s*=\s*(?:"([^"]+)"|'([^']+)')""", RegexOption.MULTILINE)
        .find(versionsToml)
        ?.groupValues
        ?.drop(1)
        ?.firstOrNull { it.isNotEmpty() }
        ?: throw GradleException("Missing version alias 'snakeyaml' in libs.versions.toml")

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.yaml:snakeyaml:$snakeYamlVersion")
    }
}

dependencies {
    api(project(":api"))

    annotationProcessor(libs.lombok)

    compileOnly(libs.libby.core)
    compileOnly(libs.proto)
    compileOnly(libs.guava)
    compileOnly(libs.cirrus.api)
    compileOnly(libs.lamp.common)
    compileOnly(libs.snakeyaml)

    compileOnly(libs.httpclient5)
    compileOnly(libs.java.websocket)

    compileOnly(libs.adventure.api)
    compileOnly(libs.gson)
    compileOnly(libs.netty.all)

    testImplementation(libs.lamp.common)
    testImplementation(libs.proto)
    // proto's gencode is stamped at protobuf.java.version; override the proto POM's transitive
    // protobuf-java so the test runtime is not older than the linked gencode version.
    testImplementation(libs.protobuf.java)
    testImplementation(libs.protobuf.util)
    testImplementation(libs.cirrus.api)
    testImplementation(libs.packetevents.api)
    testImplementation(libs.java.websocket)
    testImplementation(libs.httpclient5)
    testImplementation(libs.adventure.api)
    testImplementation(libs.adventure.serializer.legacy) // We need this because cirrus still needs these classes
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.gson)
    testImplementation(libs.snakeyaml)
    testRuntimeOnly(libs.junit.platform.launcher)
}

// PluginInfo.java template filtering (replaces Maven templating-maven-plugin)
val pluginProps = mapOf(
    "plugin" to mapOf(
        "id" to "chamosmp-modl",
        "name" to "ChamoSMP modl",
        "version" to project.version,
        "authors" to arrayOf("ChamoSMP Development Team", "Chamogelastos", "byteful", "tigerbong"),
        "description" to project.description,
        "url" to "https://github.com/ChamoSMP-modl/minecraft",
    )
)

val generateTemplates = tasks.register<Copy>("generateTemplates") {
    from("src/main/java-templates")
    into(layout.buildDirectory.dir("generated/sources/templates/java/main"))
    expand(pluginProps)
    filteringCharset = "UTF-8"
}

sourceSets.main {
    java.srcDir(generateTemplates)
}

tasks.test {
    useJUnitPlatform()
}