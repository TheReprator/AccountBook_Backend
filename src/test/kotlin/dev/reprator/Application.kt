package dev.reprator

import dev.reprator.testModule.configureCoreModule
import io.ktor.server.application.Application

fun Application.module() {
    configureCoreModule()
}