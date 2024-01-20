import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version libs.versions.kotlin
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(projects.modals)
    implementation(projects.lib.base)
    implementation(projects.lib.baseKtor)

    implementation(projects.api.country)

    implementation(libs.exposed.postgres)
    implementation(libs.exposed.dao)
    implementation(libs.koin.ktor)

    // testing
    testImplementation(projects.lib.testModule)
    testImplementation(projects.lib.commonFeatureImpl)
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