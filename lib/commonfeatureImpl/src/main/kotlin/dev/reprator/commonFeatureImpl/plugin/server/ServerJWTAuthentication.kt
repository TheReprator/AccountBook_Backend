package dev.reprator.commonFeatureImpl.plugin.server

import dev.reprator.base_ktor.exception.StatusCodeException
import dev.reprator.base.action.JwtTokenService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import org.koin.ktor.ext.inject

fun Application.configureJWTAuthentication() {

    authentication {

        jwt {

            val jwtService by this@configureJWTAuthentication.inject<JwtTokenService>()
            realm = jwtService.jwtConfiguration.realm

            verifier(jwtService.jwtVerifier)

            validate { credential ->
                jwtService.customValidator(credential)
            }

            challenge { defaultScheme, realm ->
                throw StatusCodeException.UnAuthorized(message = "Token is not valid or has expired, defaultScheme:: $defaultScheme, for realm:: $realm")
            }
        }
    }
}