import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("jvm") version libs.versions.kotlin
    alias(libs.plugins.ktor)
}

group = "dev.reprator"
version = "0.0.1"

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
    implementation(projects.lib.base)
    implementation(projects.lib.baseKtor)
    implementation(projects.lib.commonFeatureImpl)

    implementation(projects.api.language)
    implementation(projects.api.splash)
    implementation(projects.api.country)
    implementation(projects.api.userIdentity)

    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.logging)
    implementation(libs.ktor.server.config.yaml)

    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)
    implementation(libs.ktor.logback)

    // testing
    testImplementation(projects.lib.testModule)
    testImplementation(libs.test.ktor.server)
}

ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
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