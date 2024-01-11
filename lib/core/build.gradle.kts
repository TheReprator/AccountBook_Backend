import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.ksp)
}

// KSP - To use generated sources
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(libs.ktor.server.core)
    api(libs.exposed.core)
    api(libs.exposed.jodatime)

    api(libs.ktor.client.core)
    api(libs.ktor.server.auth.jwt)
    //For testing of api, else we jackson response get parsing error
    api(libs.ktor.server.serialization)


    api(libs.koin.ktor)
    api(libs.koin.core)
    api(libs.koin.annotation)
    api(libs.koin.logger)
    api(libs.ksp.processing.api)
    ksp(libs.koin.compiler)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "${JavaVersion.VERSION_17}"
        }
    }
}