package dev.reprator.commonFeatureImpl

import dev.reprator.commonFeatureImpl.plugin.server.*
import io.ktor.server.application.*

fun Application.setupServerPlugin() {
    configureCors()

    configureServerShutDown()

    configureJWTAuthentication()
    configureContentNegotiation()
    configureStatusPage()
}