package dev.reprator.plugins

import dev.reprator.core.exception.StatusCodeException
import dev.reprator.core.usecase.FailDTOResponse
import dev.reprator.core.util.constants.ERROR_DESCRIPTION_NOT_FOUND
import dev.reprator.core.util.constants.ERROR_DESCRIPTION_UNKNOWN
import dev.reprator.country.controller.routeCountry
import dev.reprator.language.controller.routeLanguage
import dev.reprator.splash.controller.routeSplash
import dev.reprator.splash.setUpSplashFolder
import dev.reprator.userIdentity.controller.routeUserIdentity
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.application.*

fun Application.configureRouting() {

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            if (cause is StatusCodeException)
            {
                call.respond(HttpStatusCode(cause.statusCode.value , ""),
                    FailDTOResponse(cause.statusCode.value, cause.message.orEmpty())
                )
            }
            else {
                call.respond(
                    HttpStatusCode(HttpStatusCode.InternalServerError.value, ""),
                    FailDTOResponse(HttpStatusCode.InternalServerError.value, "500: ${cause.message}")
                )
            }
        }

        status(HttpStatusCode.NotFound, HttpStatusCode.Forbidden) { call, status ->
            val message = when (status) {
                HttpStatusCode.NotFound -> ERROR_DESCRIPTION_NOT_FOUND
                else -> ERROR_DESCRIPTION_UNKNOWN
            }
            call.respond(HttpStatusCode(status.value , ""),
                FailDTOResponse(status.value, message))
        }
    }

    routing {
        routeSplash(environment?.config?.setUpSplashFolder())
        routeLanguage()
        routeCountry()
        routeUserIdentity()
    }
}
