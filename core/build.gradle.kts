import java.security.MessageDigest
import java.util.*

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
    java.srcDir(layout.buildDirectory.dir("generated/sources/libraryVersions"))
}

tasks {
    test {
        dependsOn("generateLibraryVersions")
        useJUnitPlatform()
    }
    compileJava {
        dependsOn("generateLibraryVersions")
    }
}

data class Library(
    val constantName: String,
    val group: String,
    val artifact: String,
    val versionAlias: String,
)

val allowedLibraries = listOf(
    Library("HTTPCLIENT5", "org.apache.httpcomponents.client5", "httpclient5", "httpclient5"),
    Library("HTTPCORE5", "org.apache.httpcomponents.core5", "httpcore5", "httpcore5"),
    Library("HTTPCORE5_H2", "org.apache.httpcomponents.core5", "httpcore5-h2", "httpcore5"),
    Library("JAVA_WEBSOCKET", "org.java-websocket", "Java-WebSocket", "java-websocket"),
    Library("LAMP_COMMON", "io.github.revxrsal", "lamp.common", "lamp"),
    Library("LAMP_BRIGADIER", "io.github.revxrsal", "lamp.brigadier", "lamp"),
    Library("LAMP_BUKKIT", "io.github.revxrsal", "lamp.bukkit", "lamp"),
    Library("LAMP_VELOCITY", "io.github.revxrsal", "lamp.velocity", "lamp"),
    Library("CIRRUS_SPIGOT", "gg.modl.minecraft.cirrus", "cirrus-spigot", "cirrus"),
    Library("CIRRUS_VELOCITY", "gg.modl.minecraft.cirrus", "cirrus-velocity", "cirrus"),
    Library("PROTOBUF_JAVA", "com.google.protobuf", "protobuf-java", "protobuf-java"),
    Library("PROTOBUF_JAVA_UTIL", "com.google.protobuf", "protobuf-java-util", "protobuf-java"),
    Library("FAILUREACCESS", "com.google.guava", "failureaccess", "failureaccess"),
    Library("PROTOVALIDATE", "build.buf", "protovalidate", "protovalidate"),
    Library("CEL_CORE", "org.projectnessie.cel", "cel-core", "cel"),
    Library("CEL_GENERATED_ANTLR", "org.projectnessie.cel", "cel-generated-antlr", "cel"),
    Library("CEL_GENERATED_PB", "org.projectnessie.cel", "cel-generated-pb", "cel"),
    Library("AGRONA", "org.agrona", "agrona", "agrona"),
    Library("IPADDRESS", "com.github.seancfoley", "ipaddress", "ipaddress"),
    Library("JAKARTA_MAIL_API", "jakarta.mail", "jakarta.mail-api", "jakarta-mail"),
    Library("JAKARTA_ACTIVATION_API", "jakarta.activation", "jakarta.activation-api", "jakarta-activation"),
    Library("MODL_PROTO", "gg.modl", "proto", "proto"),
)


afterEvaluate {
    val versionCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")

    fun catalogVersion(alias: String): String? =
        versionCatalog.findVersion(alias).orElseThrow {
            GradleException("Missing version alias '$alias' in libs.versions.toml")
        }.requiredVersion

    fun resolveChecksum(group: String, artifact: String, version: String): String {
        val coordinate = "$group:$artifact:$version"
        val dependency = dependencies.create(coordinate)
        val file = configurations.detachedConfiguration(dependency).apply {
            isTransitive = false
            resolutionStrategy.useGlobalDependencySubstitutionRules = false
        }.singleFile

        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return Base64.getEncoder().encodeToString(digest.digest())
    }

    val checksumMap: Map<String, String> = allowedLibraries.associate { lib ->
        val version = catalogVersion(lib.versionAlias) ?: throw GradleException("Version catalog is not yet initiated")
        lib.constantName to resolveChecksum(lib.group, lib.artifact, version)
    }

    tasks.register("generateLibraryVersions") {
        val outputDir = layout.buildDirectory.dir("generated/sources/libraryVersions/gg/modl/minecraft/core")

        inputs.property("versions", allowedLibraries.associate { it.constantName to catalogVersion(it.versionAlias) })
        inputs.property("checksums", checksumMap)
        inputs.property("constants", allowedLibraries.map { it.constantName })

        outputs.dir(outputDir)

        doLast {
            val generatedFile = outputDir.get().file("LibraryVersions.java").asFile
            generatedFile.parentFile.mkdirs()

            val sb = StringBuilder()
            sb.appendLine("package gg.modl.minecraft.core;")
            sb.appendLine()
            sb.appendLine("final class LibraryVersions {")
            sb.appendLine()
            sb.appendLine("    private LibraryVersions() {}")
            sb.appendLine()

            val versions = inputs.properties["versions"] as Map<*, *>
            val checksums = inputs.properties["checksums"] as Map<*, *>
            val constants = inputs.properties["constants"] as List<*>

            constants.forEach { constant ->
                val name = constant as String
                val version = versions[name]
                val checksum = checksums[name]

                sb.appendLine("""    static final String $name = "$version";""")
                sb.appendLine("""    static final String ${name}_CHECKSUM = "$checksum";""")
                sb.appendLine()
            }
            sb.appendLine("}")
            generatedFile.writeText(sb.toString())
        }
    }
}