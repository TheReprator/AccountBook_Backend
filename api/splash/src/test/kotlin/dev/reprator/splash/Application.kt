package dev.reprator.splash

import dev.reprator.commonFeatureImpl.setupServerPlugin
import dev.reprator.splash.controller.routeSplash
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {

    setupServerPlugin()

    routing {
        routeSplash()
    }
}
