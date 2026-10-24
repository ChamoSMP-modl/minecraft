plugins {
    id("sub")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":core"))
}