package dev.reprator.country

import dev.reprator.commonFeatureImpl.setupServerPlugin
import dev.reprator.country.controller.routeCountry
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {

    setupServerPlugin()

    routing {
        routeCountry()
    }
}

