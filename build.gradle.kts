plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.ktor)
}

group = "dev.reprator"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.common)
    implementation(libs.ktor.server.status.page)
    implementation(libs.ktor.server.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.logback)

    implementation(libs.kodein.mvc)

    runtimeOnly("org.postgresql:postgresql:42.6.0")
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.hikariCp)
    implementation(libs.exposed.encache)

    testImplementation(libs.ktor.test.server)
    testImplementation(libs.ktor.test.junit)
}