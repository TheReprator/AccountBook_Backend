package dev.reprator.commonFeatureImpl.plugin.client

import dev.reprator.base.action.AppLogger
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
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

                logger.e { "vikramTest:: koinTest:: validateResponse:: $failureReason" }
                throw HttpExceptions(
                    response = response,
                    failureReason = failureReason,
                    cachedResponseText = response.bodyAsText(),
                )
            }
        }

        handleResponseExceptionWithRequest { exception, _ ->
            val clientException =
                exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
            val exceptionResponse = clientException.response
            val exceptionResponseText = exceptionResponse.bodyAsText()

            logger.e { "vikramTest:: koinTest:: handleResponseExceptionWithRequest:: $exceptionResponse" }
            logger.e { "vikramTest:: koinTest:: handleResponseExceptionWithRequest1:: $exceptionResponseText" }

            throw HttpExceptions(
                response = exceptionResponse,
                failureReason = exceptionResponseText,
                cachedResponseText = exceptionResponseText,
            )
        }
    }
}

class HttpExceptions(
    response: HttpResponse,
    failureReason: String?,
    cachedResponseText: String,
) : ResponseException(response, cachedResponseText) {
    override val message: String = "Status: ${response.status}" + " Failure: $failureReason"
}