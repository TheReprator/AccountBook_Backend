package dev.reprator.testModule.plugin

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import dev.reprator.core.util.api.HttpExceptions
import dev.reprator.core.util.constants.*
import dev.reprator.core.util.logger.AppLogger
import dev.reprator.testModule.*
import dev.reprator.testModule.di.TOKEN_ACCESS
import dev.reprator.testModule.di.TOKEN_REFRESH
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.util.*
import java.lang.reflect.Type

private const val MILLISECONDS = 1000L

fun pluginClientResponseModalTransformation(mapper: ObjectMapper, reflectionType: Types): ClientPlugin<Unit> {

    return createClientPlugin("ResponseModalTransformation") {

        transformResponseBody { response, _, requestedType ->

            fun <T> responseParent(): suspend (Class<T>) -> T? = {
                try {
                    val envelopeType: Type = reflectionType.newParameterizedType(it, requestedType.reifiedType)
                    val javaType: JavaType = mapper.constructType(envelopeType)
                    mapper.readValue(response.bodyAsText(), javaType)
                } catch (e: IllegalArgumentException) {
                    println("exception IllegalArgumentException localizedMessage")
                    mapper.readValue(response.bodyAsText(), it)
                } catch (e: Exception) {
                    println("exception localizedMessage")
                    e.stackTrace
                    null
                }
            }

            if (response.status.isSuccess()) {
                responseParent<EnvelopeResponse<*>>()(EnvelopeResponse::class.java)?.data
            } else {
                response.bodyAsText()
            }
        }
    }
}

fun pluginClientLogging(httpClientConfig: HttpClientConfig<*>) {

    httpClientConfig.install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
        sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
}

fun pluginClientAuth(httpClientConfig: HttpClientConfig<*>, logger: AppLogger) {

    httpClientConfig.install(Auth) {
        bearer {

            loadTokens {
                logger.e { "vikram:: install :: loadToken, $TOKEN_ACCESS, $TOKEN_REFRESH" }
                BearerTokens(TOKEN_ACCESS, TOKEN_REFRESH)
            }

            refreshTokens {
                logger.e { "vikram:: install :: refreshTokens, $TOKEN_ACCESS, $TOKEN_REFRESH" }
                BearerTokens(TOKEN_ACCESS, TOKEN_REFRESH)
            }

            sendWithoutRequest { request ->
                logger.e { "vikram:: install :: sendWithoutRequest:: ${request.url.host}" }
                request.url.host == "www.googleapis.com"
            }
        }
    }
}

fun pluginClientContentNegotiation(httpClientConfig: HttpClientConfig<*>, mapper: ObjectMapper) {

    httpClientConfig.install(ContentNegotiation) {
        jackson {
            mapper
        }
    }
}

fun pluginClientHttpTimeout(httpClientConfig: HttpClientConfig<*>) {
    httpClientConfig.install(HttpTimeout) {
        connectTimeoutMillis = 60 * MILLISECONDS
        socketTimeoutMillis = 60 * MILLISECONDS
        requestTimeoutMillis = 60 * MILLISECONDS
    }
}

fun pluginClienDefaultRequest(httpClientConfig: HttpClientConfig<*>, clientAttributes: Attributes, serverPort: Int) {
    httpClientConfig.defaultRequest {

        val providerType = clientAttributes[AttributeKey<APIS>(API_HOST_IDENTIFIER)]

        val isExternal = providerType == APIS.EXTERNAL_OTP_VERIFICATION

        headers {
            append(HttpHeaders.ContentType, "application/json")

            if (isExternal) {
                append("API-Key", VERIFICATION_SMS_PHONE_APIKEY)
                append("User-ID", VERIFICATION_SMS_PHONE_USERID)
            }
        }

        url {
            val apiHost = when (providerType) {
                APIS.EXTERNAL_OTP_VERIFICATION ->
                    API_BASE_URL.EXTERNAL_OTP_VERIFICATION.value

                else -> API_BASE_URL.INTERNAL_APP.value
            }
            host = apiHost
            port = if (isExternal)
                0
            else
                serverPort
            protocol = if (isExternal) URLProtocol.HTTPS else URLProtocol.HTTP
        }
    }
}

fun pluginClientHttpResponseValidator(httpClientConfig: HttpClientConfig<*>, logger: AppLogger) {
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

                val responseBodyTest = response.bodyAsText()
                logger.e { "vikramTest:: koinTest:: validateResponse:: $response" }
                logger.e { "vikramTest:: koinTest:: validateResponse1:: $failureReason" }
                logger.e { "vikramTest:: koinTest:: validateResponse1:: $responseBodyTest" }

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

private fun MockRequestHandleScope.errorResponse(): HttpResponseData {
    return respond(
        content = "",
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}


@JsonInclude(JsonInclude.Include.NON_NULL)
class EnvelopeResponse<T>(
    val statusCode: Int,
    val data: T
)