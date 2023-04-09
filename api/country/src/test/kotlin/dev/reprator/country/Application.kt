package dev.reprator.country

import dev.reprator.country.controller.routeCountry
import dev.reprator.testModule.configureCoreModule
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {

    configureCoreModule()

    routing {
        routeCountry()
    }
}

