package dev.reprator.userIdentity

import dev.reprator.country.controller.routeCountry
import dev.reprator.testModule.configureCoreModule
import dev.reprator.userIdentity.controller.routeUserIdentity
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {

    configureCoreModule()

    routing {
        routeCountry()
        routeUserIdentity()
    }
}

