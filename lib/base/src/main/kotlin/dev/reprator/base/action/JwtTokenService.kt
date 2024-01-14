package dev.reprator.base.action

import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.auth.jwt.*

interface JwtTokenService {

    companion object {
        const val JWT_USER_ID = "userId"
        const val SECOND_1 = 1_000L
        private const val MINUTE_1_MILLISECONDS = 60 * SECOND_1
        const val HOUR_1_MILLISECONDS = 60 * MINUTE_1_MILLISECONDS
    }

    val jwtConfiguration: JWTConfiguration

    val jwtVerifier: JWTVerifier

    suspend fun generateAccessToken(userId: String): String

    suspend fun generateRefreshToken(userId: String): String

    suspend fun customValidator(credential: JWTCredential): JWTPrincipal?

    suspend fun isTokenValid(token: String): Pair<Boolean, Int>
}

data class JWTConfiguration(val secret: String, val audience: String, val issuer: String, val realm: String)