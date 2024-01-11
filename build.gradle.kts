import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
}

group = "dev.reprator"
version = "0.0.1"

// KSP - To use generated sources
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

ksp {
    arg("KOIN_CONFIG_CHECK","true")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

ktor {
    fatJar {
        archiveFileName.set("Reprator-cashBook.jar")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(projects.api.language)
    implementation(projects.api.splash)
    implementation(projects.api.country)
    implementation(projects.api.userIdentity)
    implementation(projects.lib.core)

    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.common)
    implementation(libs.ktor.server.status.page)
    implementation(libs.ktor.server.logging)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.serialization)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.config.yaml)
    implementation(libs.ktor.logback)

    runtimeOnly(libs.exposed.postgres)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.hikariCp)

//    implementation(libs.koin.ktor)
//    implementation(libs.koin.core)
//    implementation(libs.koin.annotation)
//    implementation(libs.koin.logger)
//    implementation(libs.ksp.processing.api)
    ksp(libs.koin.compiler)

    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)

    // testing
    testImplementation(projects.lib.testModule)
    testImplementation(libs.test.ktor.server)
}

tasks {

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "${JavaVersion.VERSION_17}"
        }
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
    }

}