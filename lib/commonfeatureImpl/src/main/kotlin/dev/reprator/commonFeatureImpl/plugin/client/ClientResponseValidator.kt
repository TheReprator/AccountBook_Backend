package dev.reprator.commonFeatureImpl.plugin.client

import com.fasterxml.jackson.databind.ObjectMapper
import dev.reprator.base.action.AppLogger
import dev.reprator.base.beans.APIS
import dev.reprator.base.beans.API_HOST_IDENTIFIER
import dev.reprator.base.usecase.FailDTOResponse
import dev.reprator.base_ktor.api.HttpExceptions
import dev.reprator.base_ktor.api.OwnServerErrorException
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import org.koin.core.Koin

fun pluginClientResponseValidator(koin: Koin, httpClientConfig: HttpClientConfig<*>) {

    val logger by koin.inject<AppLogger>()

    httpClientConfig.HttpResponseValidator {
        validateResponse { response ->

            if (!response.status.isSuccess()) {
                val failureReason = when (response.status) {
                    HttpStatusCode.Unauthorized -> "Unauthorized request"
                    HttpStatusCode.Forbidden -> "${response.status.value} Missing API key."
                    HttpStatusCode.NotFound -> "Invalid Request"
                    HttpStatusCode.UpgradeRequired -> "Upgrade to VIP"
                    HttpStatusCode.RequestTimeout -> "Network Timeout"
                    in HttpStatusCode.InternalServerError..HttpStatusCode.GatewayTimeout ->
                        "${response.status.value} Server Error"

                    else -> "Network error!"
                }

                logger.e { "vikramTest:: validateResponse:: $failureReason" }

                throw mapResponse(koin, response, failureReason)
            }
        }

        handleResponseExceptionWithRequest { exception, _ ->
            val clientException =
                exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest

            val exceptionResponse = clientException.response

            logger.e { "vikramTest:: handleResponseExceptionWithRequest:: $exceptionResponse" }
            throw mapResponse(koin, exceptionResponse)
        }
    }
}


suspend fun mapResponse(koin: Koin, response: HttpResponse, failureReason: String ?= null): Exception {

    val clientAttributes: Attributes = koin.get()
    val providerType = clientAttributes[AttributeKey<APIS>(API_HOST_IDENTIFIER)]

    if (providerType == APIS.INTERNAL_APP) {
        try {
            val mapper: ObjectMapper = koin.get()
            val mappedValue = mapper.readValue(response.bodyAsText(), FailDTOResponse::class.java)
            return OwnServerErrorException(mappedValue)
        } catch (_: Exception) {
        }
    }

    return HttpExceptions(
        response = response,
        failureReason = failureReason ?: response.bodyAsText(),
        cachedResponseText = response.bodyAsText(),
    )
}