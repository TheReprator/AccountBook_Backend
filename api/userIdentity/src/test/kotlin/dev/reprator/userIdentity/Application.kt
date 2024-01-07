package dev.reprator.userIdentity

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.reprator.core.exception.StatusCodeException
import dev.reprator.core.usecase.JwtTokenService
import dev.reprator.country.controller.routeCountry
import dev.reprator.testModule.*
import dev.reprator.userIdentity.controller.routeUserIdentity
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.module() {

    configureJWTAuthentication()
    configureCoreModule()

    routing {
        routeCountry()
        routeUserIdentity()
    }
}


fun Application.configureJWTAuthentication() {

    val jwtService by inject<JwtTokenService>()

    authentication {

        jwt {
            realm = JWT_PARAMETER_REALM

            verifier(
                JWT.require(Algorithm.HMAC256(JWT_PARAMETER_SECRET))
                    .withAudience(JWT_PARAMETER_AUDIENCE)
                    .withIssuer(JWT_PARAMETER_ISSUER)
                    .build()
            )

            validate { credential ->
                jwtService.customValidator(credential)
            }

            challenge { defaultScheme, realm ->
                throw StatusCodeException.UnAuthorized(message = "Token is not valid or has expired, defaultScheme:: $defaultScheme, for realm:: $realm")
            }
        }
    }
}
