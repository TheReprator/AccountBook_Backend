package dev.reprator.commonFeatureImpl.plugin.client

import dev.reprator.base.action.AppLogger
import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import org.koin.core.Koin

var TOKEN_ACCESS: String = ""
var TOKEN_REFRESH: String = ""

fun pluginClientResponseAuth(koin: Koin, httpClientConfig: HttpClientConfig<*>) {

    httpClientConfig.install(Auth) {
        bearer {
            val logger by koin.inject<AppLogger>()

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
