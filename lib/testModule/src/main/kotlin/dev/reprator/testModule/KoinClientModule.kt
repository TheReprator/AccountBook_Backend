package dev.reprator.testModule

import dev.reprator.core.util.api.HttpExceptions
import dev.reprator.core.util.constants.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.util.*
import org.koin.dsl.module

private const val MILLISECONDS = 1000L

val koinAppTestNetworkModule = module {
    single<List<MockClientResponseHandler>> { emptyList() }

    single<Attributes> { Attributes(true) }

    single<HttpClient> {
        val engine = MockEngine { request ->

            val injectedHandlers: List<MockClientResponseHandler> = get()

            injectedHandlers.forEach { handler ->
                val response = handler.handleRequest(this, request)
                if (response != null) {
                    return@MockEngine response
                }
            }
            return@MockEngine errorResponse()
        }

        val clientAttributes: Attributes = get()

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

                val providerType = clientAttributes[AttributeKey<APIS>(API_HOST_IDENTIFIER)]

                headers {
                    append(HttpHeaders.ContentType, "application/json")

                    val apiType = providerType == APIS.EXTERNAL_OTP_VERIFICATION
                    if(apiType) {
                        append("API-Key", VERIFICATION_SMS_PHONE_API)
                        append("User-ID", VERIFICATION_SMS_PHONE_USERID)
                    }
                }

                url {
                    val apiHost = when(providerType) {
                        APIS.EXTERNAL_OTP_VERIFICATION ->
                            API_BASE_URL.EXTERNAL_OTP_VERIFICATION.value
                        else -> API_BASE_URL.INTERNAL_APP.value
                    }
                    host = apiHost
                    port = 8081
                    protocol = if(providerType == APIS.EXTERNAL_OTP_VERIFICATION) URLProtocol.HTTPS else URLProtocol.HTTP
                }
            }


            HttpResponseValidator {
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

                        throw HttpExceptions(
                            response = response,
                            failureReason = failureReason,
                            cachedResponseText = response.bodyAsText(),
                        )
                    }
                }
            }
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