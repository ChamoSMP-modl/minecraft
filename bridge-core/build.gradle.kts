plugins {
    id("sub")
}

dependencies {
    compileOnlyApi(project(":core"))

    compileOnly(libs.snakeyaml)

    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.minimessage)
    compileOnly(libs.netty.all)
    compileOnly(libs.packetevents.api)
    compileOnly(libs.gson)

    annotationProcessor(libs.lombok)

    testImplementation(project(":core"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.gson)
    testImplementation(libs.snakeyaml)
    testImplementation(libs.netty.all)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
