plugins {
    id("sub")
}

dependencies {
    compileOnlyApi(libs.lombok)
    annotationProcessor(libs.lombok)

    compileOnly(libs.annotations)
    compileOnly(libs.gson)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}