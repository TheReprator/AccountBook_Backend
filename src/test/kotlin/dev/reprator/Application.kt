package dev.reprator

import dev.reprator.commonFeatureImpl.setupServerPlugin
import io.ktor.server.application.Application

fun Application.module() {
    setupServerPlugin()
}