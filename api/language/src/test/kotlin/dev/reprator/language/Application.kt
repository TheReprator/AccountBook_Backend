package dev.reprator.language

import dev.reprator.language.controller.routeLanguage
import dev.reprator.testModule.configureCoreModule
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.module() {

    configureCoreModule()

    routing {
        routeLanguage()
    }
}
