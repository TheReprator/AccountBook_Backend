package dev.reprator.userIdentity.socialVerifier

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

class SMScodeGeneratorTestSuccessImpl(private val client: HttpClient, private val attributes: Attributes, private val appLogger: AppLogger) : SMScodeGenerator {

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

        appLogger.e { "ResponseSucc : $response" }
        return when (response) {
            is ApiResponse.Success -> {
                response.body.sent
            }

            else -> false
        }
    }
}

class SMScodeGeneratorTestFailImpl(private val client: HttpClient, private val attributes: Attributes, private val appLogger: AppLogger) : SMScodeGenerator {

    override suspend fun sendOtpToMobileNumber(countryCode: Int, phoneNumber: String, messageCode: Int): Boolean {
        val response: ApiResponse<AuthServiceEntity> = client.safeRequest(apiType= APIS.EXTERNAL_OTP_VERIFICATION, attributes = attributes) {
            url {
                method = HttpMethod.Post
                path(VERIFY_SMS)
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    FormDataContent(
                        parameters {
                            append("security-code", "$messageCode")
                            append("language-code", "en")
                            append("code-length", LENGTH_OTP.toString())
                            append("brand-name", "Reprator-Cashbook")
                        })
                )
            }
        }
        appLogger.e { "ResponseFail : $response" }
        return when (response) {
            is ApiResponse.Success -> {
                response.body.sent
            }

            else -> false
        }
    }
}