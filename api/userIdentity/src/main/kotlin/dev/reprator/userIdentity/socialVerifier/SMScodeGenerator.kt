package dev.reprator.userIdentity.socialVerifier

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import dev.reprator.core.util.api.ApiResponse
import dev.reprator.core.util.api.safeRequest
import dev.reprator.core.util.logger.AppLogger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

private const val ENDPOINT_SERVER_PHONE_SMS = "neutrinoapi.net"
private const val VERIFY_SMS = "/sms-verify"
private const val VERIFY_PHONE = "/phone-verify"


interface SMScodeGenerator {

    suspend fun generateCode(): Int

    suspend fun sendTokenToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean
}

class SMScodeGeneratorImpl(private val client: HttpClient, private val appLogger: AppLogger, private val scope: CoroutineScope) : SMScodeGenerator {

    override suspend fun generateCode(): Int {
        val random = Random.nextInt(999999)
        return String.format("%06d", random).toInt()
    }

    override suspend fun sendTokenToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean {
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