package dev.reprator.userIdentity.socialVerifier

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty
import dev.reprator.core.util.api.ApiResponse
import dev.reprator.core.util.api.safeRequest
import dev.reprator.core.util.constants.APIS
import dev.reprator.core.util.constants.LENGTH_OTP
import dev.reprator.core.util.logger.AppLogger
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*


internal const val VERIFY_SMS = "/sms-verify"
private const val VERIFY_PHONE = "/phone-verify"


interface SMScodeGenerator {
    suspend fun sendOtpToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean
}

class SMScodeGeneratorImpl(private val client: HttpClient, private val attributes: Attributes, private val appLogger: AppLogger) : SMScodeGenerator {


    override suspend fun sendOtpToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean {
        val response: ApiResponse<AuthServiceEntity> = client.safeRequest(apiType= APIS.EXTERNAL_OTP_VERIFICATION, attributes = attributes) {
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
                            append("code-length", LENGTH_OTP.toString())
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