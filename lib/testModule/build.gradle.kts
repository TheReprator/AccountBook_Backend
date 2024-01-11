import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
}

// KSP - To use generated sources
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(libs.ktor.server.core)
    api(libs.exposed.core)

    // testing
    api(project(mapOf("path" to ":lib:core")))

    api(libs.test.junit5)
    api(libs.test.junit5.suite)
    api(libs.test.junit5.runtime)
    api(libs.test.ktor.server)

    api(libs.test.koin)
    api(libs.test.koin.junit5)
    ksp(libs.koin.compiler)

    api(libs.test.kotlin)

    api(libs.ktor.client.auth)
    api(libs.ktor.client.mock)

    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.common)
    implementation(libs.ktor.server.status.page)
    implementation(libs.ktor.server.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization)

    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.hikariCp)
    implementation(libs.exposed.h2Db)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "${JavaVersion.VERSION_17}"
        }
    }
}