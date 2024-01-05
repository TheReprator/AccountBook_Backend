package dev.reprator.testModule

import dev.reprator.core.exception.StatusCodeException
import dev.reprator.core.usecase.FailDTOResponse
import dev.reprator.core.util.constants.ERROR_DESCRIPTION_NOT_FOUND
import dev.reprator.core.util.constants.ERROR_DESCRIPTION_UNKNOWN
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
            if (cause is StatusCodeException) {
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
}
