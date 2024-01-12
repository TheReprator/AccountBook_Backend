import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(projects.lib.baseKtor)
    implementation(projects.lib.base)
    implementation(projects.lib.commonFeatureImpl)

    implementation(libs.ktor.server.netty)
    api(libs.ktor.server.core)
    api(libs.exposed.core)

    api(libs.test.junit5)
    api(libs.test.junit5.suite)
    api(libs.test.junit5.runtime)
    api(libs.test.koin)
    api(libs.test.koin.junit5)
    api(libs.test.kotlin)

    api(libs.ktor.client.mock)

    implementation(libs.exposed.h2Db)
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "${JavaVersion.VERSION_17}"
        }
    }
}