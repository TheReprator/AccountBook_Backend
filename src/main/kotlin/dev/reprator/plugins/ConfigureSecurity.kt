package dev.reprator.plugins

import dev.reprator.core.exception.StatusCodeException
import dev.reprator.core.usecase.JwtTokenService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

fun Application.configureJWTAuthentication() {

    val jwtService by inject<JwtTokenService>()

    authentication {
        jwt {
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