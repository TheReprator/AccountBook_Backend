package dev.reprator.base.action

import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.auth.jwt.*

interface JwtTokenService {

    companion object {
        const val JWT_USER_ID = "userId"
        private const val SECOND_1 = 1_000L
        private const val MINUTE_1 = 60 * SECOND_1
        private const val HOUR_1 = 60 * MINUTE_1
        //private const val tokenExpiration: Long = 12 * HOUR_1   // 1 day
        //private const val refreshTokenExpiration: Long = 2 * HOUR_1 // 2 day
        private const val tokenExpiration: Long = 1 * SECOND_1   // 1 day
        private const val refreshTokenExpiration: Long =  2 * SECOND_1 // 2 day
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