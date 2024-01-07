package dev.reprator.impl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import dev.reprator.core.usecase.JWTConfiguration
import dev.reprator.core.usecase.JwtTokenService
import dev.reprator.core.usecase.JwtTokenService.Companion.JWT_USER_ID
import dev.reprator.userIdentity.data.UserIdentityRepository
import io.ktor.server.auth.jwt.*
import java.util.*

private const val JWT_RANDOM_UUID = "randomUUID"

class JwtTokenServiceImpl(
    override val jwtConfiguration: JWTConfiguration,
    private val userController: UserIdentityRepository
) : JwtTokenService {

    private fun createJWTToken(userId: String, time: Long): String = JWT.create()
        .withAudience(jwtConfiguration.audience)
        .withIssuer(jwtConfiguration.issuer)
        .withClaim(JWT_USER_ID, userId)
        .withClaim(JWT_RANDOM_UUID, UUID.randomUUID().toString())
        .withExpiresAt(Date(System.currentTimeMillis() + time))
        .sign(Algorithm.HMAC256(jwtConfiguration.secret))

    override suspend fun generateAccessToken(userId: String, expirationPeriod: Long) =
        createJWTToken(userId, expirationPeriod)

    override suspend fun generateRefreshToken(userId: String, expirationPeriod: Long) =
        createJWTToken(userId, expirationPeriod)

    override val jwtVerifier: JWTVerifier = JWT.require(Algorithm.HMAC256(jwtConfiguration.secret))
        .withAudience(jwtConfiguration.audience)
        .withIssuer(jwtConfiguration.issuer)
        .build()

    override suspend fun customValidator(credential: JWTCredential): JWTPrincipal? {
        val userId = credential.payload.getClaim(JWT_USER_ID).asString() ?: return null

        if (!credential.payload.audience.contains(jwtConfiguration.audience)) {
            return null
        }

        return try {
            val fullModal = userController.getUserById(userId.toInt())
            if(fullModal.refreshToken.trim().isNotEmpty())
                JWTPrincipal(credential.payload)
            else
                null
        } catch (exception: Exception) {
            null
        }
    }

    override suspend fun isTokenValid(token: String): Pair<Boolean, Int> {
        val decodedJwt = jwtVerifier.verify(token) ?: return false to -1
        if (decodedJwt.expiresAt <= Date())
            return false to -1
        val audienceMatches = jwtConfiguration.audience == (decodedJwt.audience.first())
        if (audienceMatches) {
            val isUserIdExist = decodedJwt.getClaim(JWT_USER_ID).asString()
            if (true == isUserIdExist?.trim()?.isNotEmpty())
                return true to isUserIdExist.toInt()
        }
        return false to -1
    }

}
