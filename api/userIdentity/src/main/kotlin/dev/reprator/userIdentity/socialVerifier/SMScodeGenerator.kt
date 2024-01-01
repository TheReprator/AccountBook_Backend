package dev.reprator.userIdentity.socialVerifier

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import dev.reprator.core.AppLogger
import dev.reprator.userIdentity.socialVerifier.util.ApiResponse
import dev.reprator.userIdentity.socialVerifier.util.HttpExceptions
import dev.reprator.userIdentity.socialVerifier.util.safeRequest
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

private const val ENDPOINT_SERVER_PHONE_SMS = "neutrinoapi.net"
private const val VERIFY_SMS = "/sms-verify"
private const val VERIFY_PHONE = "/phone-verify"
private const val VERIFICATION_SMS_PHONE_API = "dQzSARrkyBgkLpNKLnswaqnktfC2EvrB7IwiPCLbIUUKJtIj"
private const val VERIFICATION_SMS_PHONE_USERID = "Reprator"


private const val MILLISECONDS = 1000L

interface SMScodeGenerator {

    suspend fun generateCode(): Int

    suspend fun sendTokenToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean
}

class SMScodeGeneratorImpl(private val appLogger: AppLogger, private val scope: CoroutineScope) : SMScodeGenerator {

    override suspend fun generateCode(): Int {
        val random = Random.nextInt(999999)
        return String.format("%06d", random).toInt()
    }

    override suspend fun sendTokenToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean {
        val client = createHttpClient()
        val response: ApiResponse<AuthServiceEntity> = client.safeRequest {
            url {
                method = HttpMethod.Post
                path(VERIFY_SMS)
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    FormDataContent(
                        parameters {
                            append("number", "$countryCode$phoneNumber")
                            append("security-code", "$messageCode")
                            append("language-code", "en")
                            append("code-length", "6")
                            append("brand-name", "Reprator-Cashbook")
                        })
                )
            }
        }
        appLogger.e { "vikramResponseTest:: $response" }
        return when (response) {
            is ApiResponse.Success -> {
                response.body.sent
            }

            else -> false
        }
    }

    private fun createHttpClient(): HttpClient {
        val client = HttpClient(CIO) {

            expectSuccess = true

            engine {
                maxConnectionsCount = 1000
                endpoint {
                    maxConnectionsPerRoute = 100
                    pipelineMaxSize = 20
                    keepAliveTime = 5000
                    connectTimeout = 5000
                    connectAttempts = 5
                }
            }

//            install(HttpRequestRetry) {
//                maxRetries = 5
//                retryIf { _, response ->
//                    !response.status.isSuccess()
//                }
//                retryOnExceptionIf { _, cause ->
//                    cause is IOException
//                }
//                delayMillis { retry ->
//                    retry * 3000L
//                } // retries in 3, 6, 9, etc. seconds
//            }


            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
//                filter { request ->
//                    request.url.host.contains("ktor.io")
//                }
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }


            install(ContentNegotiation) {
                jackson()
            }


            install(HttpTimeout) {
                connectTimeoutMillis = 10 * MILLISECONDS
                socketTimeoutMillis = 10 * MILLISECONDS
                requestTimeoutMillis = 10 * MILLISECONDS
            }


            defaultRequest {

                headers {
                    append(HttpHeaders.ContentType, "application/json")
                    append("API-Key", VERIFICATION_SMS_PHONE_API)
                    append("User-ID", VERIFICATION_SMS_PHONE_USERID)
                }

                url {
                    host = ENDPOINT_SERVER_PHONE_SMS
                    protocol = URLProtocol.HTTPS
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

        return client
    }
}

data class AuthServiceEntity(
    @JsonProperty("number-valid")
    @JsonAlias("numberValid")
    val numberValid: Boolean,
    @JsonProperty("securityCode")
    @JsonAlias("security-code")
    val securityCode: String,
    @JsonProperty("sent")
    val sent: Boolean
)