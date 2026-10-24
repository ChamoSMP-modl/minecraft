plugins {
    id("sub")
    alias(libs.plugins.shadow)

    alias(libs.plugins.run.paper)
    alias(libs.plugins.run.velocity)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":bridge-core"))
    implementation(project(":platforms:spigot"))
    implementation(project(":platforms:spigot-sv"))
    implementation(project(":platforms:velocity"))
}

tasks.shadowJar {
    archiveBaseName.set("chamosmp-modl")
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
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}