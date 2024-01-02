package dev.reprator.testModule

import dev.reprator.core.util.api.HttpExceptions
import dev.reprator.core.util.constants.*
import dev.reprator.core.util.logger.AppLogger
import impl.AppLoggerImpl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.util.*
import org.koin.core.KoinApplication
import org.koin.dsl.module

private const val MILLISECONDS = 1000L

fun KoinApplication.setupCoreNetworkModule() {
    modules(appCoreModule, koinAppTestNetworkModule)
}

private val appCoreModule = module {
    single<AppLogger> { AppLoggerImpl() }
}

private val koinAppTestNetworkModule = module {
    single<Int> { ApplicationConfig("application-test.conf").property("ktor.deployment.port").getString().toInt() }

    single<HttpClientEngine> { CIO.create() }

    single<Attributes> { Attributes(true) }

    single<HttpClient> {

        val engine: HttpClientEngine = get()

        HttpClient(engine) {

            expectSuccess = true

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }


            install(ContentNegotiation) {
                jackson()
            }


            install(HttpTimeout) {
                connectTimeoutMillis = 60 * MILLISECONDS
                socketTimeoutMillis = 60 * MILLISECONDS
                requestTimeoutMillis = 60 * MILLISECONDS
            }


            defaultRequest {
                val clientAttributes: Attributes = get()

                val providerType = clientAttributes[AttributeKey<APIS>(API_HOST_IDENTIFIER)]

                val isExternal = providerType == APIS.EXTERNAL_OTP_VERIFICATION

                headers {
                    append(HttpHeaders.ContentType, "application/json")

                    if (isExternal) {
                        append("API-Key", VERIFICATION_SMS_PHONE_API)
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
                        get<Int>()
                    protocol = if (isExternal) URLProtocol.HTTPS else URLProtocol.HTTP
                }
            }


            HttpResponseValidator {
                val logger = get<AppLogger>()

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

                        val rp = response.bodyAsText()
                        logger.e {  "vikramTest:: koinTest:: validateResponse:: $response" }
                        logger.e {  "vikramTest:: koinTest:: validateResponse1:: $failureReason" }
                        logger.e {  "vikramTest:: koinTest:: validateResponse1:: $rp" }

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

                    logger.e { "vikramTest:: koinTest:: handleResponseExceptionWithRequest:: $exceptionResponse"}
                    logger.e { "vikramTest:: koinTest:: handleResponseExceptionWithRequest1:: $exceptionResponseText"}

                    throw HttpExceptions(
                        response = exceptionResponse,
                        failureReason = exceptionResponseText,
                        cachedResponseText = exceptionResponseText,
                    )
                }
            }
        }
    }
}

fun MockRequestHandleScope.errorResponse(): HttpResponseData {
    return respond(
        content = "",
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}