package dev.reprator.language

import dev.reprator.commonFeatureImpl.setupServerPlugin
import dev.reprator.language.controller.routeLanguage
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {

    setupServerPlugin()

    routing {
        routeLanguage()
    }
}
