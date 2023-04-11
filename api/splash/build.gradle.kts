plugins {
    kotlin("jvm") version libs.versions.kotlin
}

dependencies {
    implementation(project(":api:language"))

    implementation(libs.koin.ktor)

    // testing
    testImplementation(libs.test.ktor.server)
    testImplementation(project(":lib:testModule"))
}

tasks.test {
    // Use the built-in JUnit support of Gradle.
    useJUnitPlatform()
}
