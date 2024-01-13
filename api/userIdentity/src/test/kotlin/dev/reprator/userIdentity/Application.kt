package dev.reprator.userIdentity

import dev.reprator.commonFeatureImpl.setupServerPlugin
import dev.reprator.country.controller.routeCountry
import dev.reprator.userIdentity.controller.routeUserIdentity
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {

    setupServerPlugin()

    routing {
        routeCountry()
        routeUserIdentity()
    }
}
