package dev.reprator.commonFeatureImpl.plugin.client

import dev.reprator.base.action.AppLogger
import dev.reprator.base.beans.APIS
import dev.reprator.base.beans.API_BASE_URL
import dev.reprator.base.usecase.AppResult
import dev.reprator.base_ktor.api.safeRequest
import dev.reprator.modals.user.UserIdentityOTPModal
import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.*
import org.koin.core.Koin

var TOKEN_ACCESS: String = ""
var TOKEN_REFRESH: String = ""

fun pluginClientResponseAuth(koin: Koin, httpClientConfig: HttpClientConfig<*>) {

    httpClientConfig.install(Auth) {
        val logger by koin.inject<AppLogger>()

        bearer {

            sendWithoutRequest { request ->
                logger.e { "pluginAuth: sendWithoutRequest" }
                API_BASE_URL.INTERNAL_APP.value == request.host
            }

            loadTokens {
                logger.e { "pluginAuth: loadToken, $TOKEN_ACCESS, $TOKEN_REFRESH" }
                if (TOKEN_ACCESS.isNotEmpty())
                    BearerTokens(TOKEN_ACCESS, TOKEN_REFRESH)
                else
                    null
            }

            refreshTokens {
                TOKEN_ACCESS = ""
                logger.e { "pluginAuth: refreshTokens, $TOKEN_ACCESS, $TOKEN_REFRESH" }

                if (TOKEN_REFRESH.isEmpty()) {
                    return@refreshTokens null
                }

                val httpClient by koin.inject<HttpClient>()
                val attributes = koin.get<Attributes>()

                val refreshResultWrapper =
                    httpClient.safeRequest<UserIdentityOTPModal>(apiType = APIS.INTERNAL_APP, attributes = attributes) {
                        markAsRefreshTokenRequest()
                        url {
                            method = HttpMethod.Post
                            path("/accounts/refreshToken")
                            setBody(
                                FormDataContent(
                                    parameters {
                                        append("accessToken", TOKEN_REFRESH)
                                    })
                            )
                        }
                    }

                logger.e { "pluginAuth: refreshTokens result :: $refreshResultWrapper" }

                when (refreshResultWrapper) {
                    is AppResult.Success -> {
                        val body = refreshResultWrapper.body
                        TOKEN_ACCESS = body.accessToken
                        TOKEN_REFRESH = body.refreshToken
                        logger.e { "pluginAuth: refreshTokens result :: success: $TOKEN_ACCESS, ::,  $TOKEN_REFRESH " }
                        BearerTokens(TOKEN_ACCESS, TOKEN_REFRESH)
                    }

                    else -> {
                        TOKEN_REFRESH = ""

                        null
                    }
                }
            }
        }
    }
}
