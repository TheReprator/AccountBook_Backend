package dev.reprator.userIdentity.controller

import dev.reprator.base.action.JwtTokenService.Companion.JWT_USER_ID
import dev.reprator.base_ktor.util.respondWithResult
import dev.reprator.userIdentity.domain.IllegalUserIdentityException
import dev.reprator.userIdentity.domain.InvalidTokenException
import dev.reprator.userIdentity.modal.UserIdentityOtpEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.validateForNonEmpty
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val ENDPOINT_ACCOUNT = "/accounts"
const val ACCOUNT_REGISTER = "register"
const val ACCOUNT_OTP_GENERATE = "otpGenerate"
const val ACCOUNT_OTP_VERIFY = "otpVerify"
const val ACCOUNT_LOGOUT = "logout"
const val ACCOUNT_TOKEN_REFRESH = "refreshToken"

const val PARAMETER_USER_ID = "userId"
const val PARAMETER_ACCESS_TOKEN = "accessToken"

fun Routing.routeUserIdentity() {

    val controller by inject<UserIdentityController>()

    route(ENDPOINT_ACCOUNT) {

        post(ACCOUNT_REGISTER) {
            val userInfo = call.receiveNullable<UserIdentityRegisterEntity.DTO>()?.validate() ?: throw IllegalUserIdentityException()
            val userRegisterResult = controller.addNewUserIdentity(userInfo)
            respondWithResult(HttpStatusCode.Created, userRegisterResult).also {
                controller.generateAndSendOTP(userRegisterResult.userId)
            }
        }

        patch (ACCOUNT_OTP_GENERATE) {
            val userId = call.receiveParameters()[PARAMETER_USER_ID]?.toIntOrNull()?.validateForNonEmpty() ?: throw IllegalUserIdentityException()
            respondWithResult(HttpStatusCode.OK, controller.generateAndSendOTP(userId))
        }

        post(ACCOUNT_OTP_VERIFY) {
            val otpInfo = call.receiveNullable<UserIdentityOtpEntity.DTO>()?.validate() ?: throw IllegalUserIdentityException()
            respondWithResult(HttpStatusCode.OK, controller.verifyOtp(otpInfo))
        }

        post(ACCOUNT_TOKEN_REFRESH) {
            val accessToken = call.receiveParameters()[PARAMETER_ACCESS_TOKEN]
            accessToken?.validateForNonEmpty() ?: throw InvalidTokenException()
            respondWithResult(HttpStatusCode.OK, controller.refreshToken(accessToken))
        }

        authenticate {
            post(ACCOUNT_LOGOUT) {
                val jwtData = call.authentication.principal<JWTPrincipal>()
                val userId = jwtData?.getClaim(JWT_USER_ID, String::class)!!.toInt()
                respondWithResult(HttpStatusCode.OK, controller.logout(userId))
            }
        }
    }
}