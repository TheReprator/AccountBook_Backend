package dev.reprator.testModule

import dev.reprator.core.ERROR_DESCRIPTION_NOT_FOUND
import dev.reprator.core.ERROR_DESCRIPTION_UNKNOWN
import dev.reprator.core.FailResponse
import dev.reprator.core.exception.StatusCodeException
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureCoreModule() {

    install(ContentNegotiation) {
        jackson()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            if (cause is StatusCodeException)
                call.respond(FailResponse(cause.statusCode.value, cause.message.orEmpty()))
            else
                call.respond(FailResponse(HttpStatusCode.InternalServerError.value, "500: ${cause.message}"))
        }

        status(HttpStatusCode.NotFound, HttpStatusCode.Forbidden) { call, status ->
            val message = when (status) {
                HttpStatusCode.NotFound -> ERROR_DESCRIPTION_NOT_FOUND
                else -> ERROR_DESCRIPTION_UNKNOWN
            }
            call.respond(FailResponse(status.value, message))
        }
    }
}
