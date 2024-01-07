package dev.reprator.core.usecase

import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.auth.jwt.*

interface JwtTokenService {

    companion object {
        private const val SECOND_1 = 1_000L
        private const val tokenExpiration: Long = 2 * SECOND_1   // 1 day
        private const val refreshTokenExpiration: Long = 3 * SECOND_1 // 10 day
    }

    val jwtConfiguration: JWTConfiguration

    val jwtVerifier: JWTVerifier

    suspend fun generateAccessToken(
        userId: String, expirationPeriod: Long = tokenExpiration
    ): String

    suspend fun generateRefreshToken(
        userId: String, expirationPeriod: Long = refreshTokenExpiration
    ): String

    suspend fun customValidator(credential: JWTCredential): JWTPrincipal?

    suspend fun isTokenValid(token: String): Pair<Boolean, Int>
}

data class JWTConfiguration(val secret: String, val audience: String, val issuer: String, val realm: String)