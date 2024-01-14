package dev.reprator.commonFeatureImpl.plugin.client

import dev.reprator.base.action.AppLogger
import dev.reprator.base.action.TokenStorage
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

fun pluginClientResponseAuth(koin: Koin, httpClientConfig: HttpClientConfig<*>) {

    httpClientConfig.install(Auth) {
        val logger by koin.inject<AppLogger>()
        val tokenStorage by koin.inject<TokenStorage>()

        bearer {

            sendWithoutRequest { request ->
                logger.e { "pluginAuth: sendWithoutRequest" }
                API_BASE_URL.INTERNAL_APP.value == request.host
            }

            loadTokens {
                logger.e { "pluginAuth: loadToken, ${tokenStorage.accessToken}, ${tokenStorage.refresh}" }
                if (tokenStorage.accessToken.isNotEmpty())
                    BearerTokens(tokenStorage.accessToken, tokenStorage.refresh)
                else
                    null
            }

            refreshTokens {
                tokenStorage.clearAccessToken()

                logger.e { "pluginAuth: refreshTokens, ${tokenStorage.accessToken}, ${tokenStorage.refresh}" }

                if (tokenStorage.refresh.isEmpty()) {
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
                                        append("accessToken", tokenStorage.refresh)
                                    })
                            )
                        }
                    }

                logger.e { "pluginAuth: refreshTokens result :: $refreshResultWrapper" }

                when (refreshResultWrapper) {
                    is AppResult.Success -> {
                        val body = refreshResultWrapper.body
                        tokenStorage.updateToken(body.accessToken, body.refreshToken)
                        BearerTokens(body.accessToken, body.refreshToken)
                    }

                    else -> {
                        tokenStorage.cleaToken()
                        null
                    }
                }
            }
        }
    }
}
