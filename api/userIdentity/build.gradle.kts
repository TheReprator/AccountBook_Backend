import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.ksp)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(projects.lib.core)
    implementation(projects.api.country)
    implementation("com.google.api-client:google-api-client:1.35.2")

    implementation(libs.exposed.postgres)
    implementation(libs.test.coroutine)

    implementation(libs.exposed.dao)
    implementation(libs.koin.ktor)

    ksp(libs.koin.compiler)

    // testing
    testImplementation(libs.test.ktor.server)
    testImplementation(projects.lib.testModule)
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