import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(projects.lib.base)

    api(libs.ktor.server.core)

    api(libs.exposed.core)
    api(libs.exposed.jodatime)

    api(libs.ktor.client.core)
    api(libs.ktor.server.serialization.jackson)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "${JavaVersion.VERSION_17}"
        }
    }
}