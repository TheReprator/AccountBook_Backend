package dev.reprator.commonFeatureImpl.plugin.server

import dev.reprator.base.beans.ERROR_DESCRIPTION_NOT_FOUND
import dev.reprator.base.beans.ERROR_DESCRIPTION_UNKNOWN
import dev.reprator.base.usecase.FailDTOResponse
import dev.reprator.base_ktor.exception.StatusCodeException
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.application.*

fun Application.configureStatusPage() {

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
}
