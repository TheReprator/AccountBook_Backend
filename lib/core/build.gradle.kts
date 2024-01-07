import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
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
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "${JavaVersion.VERSION_17}"
        }
    }
}