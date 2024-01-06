package dev.reprator.userIdentity.controller

import dev.reprator.core.util.respondWithResult
import dev.reprator.userIdentity.domain.IllegalUserIdentityException
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.validateForNonEmpty
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val ENDPOINT_ACCOUNT = "/accounts"
const val ACCOUNT_REGISTER = "register"
const val ACCOUNT_OTP_GENERATE = "otpGenerate"
const val ACCOUNT_OTP_VERIFY = "otpVerify"
const val PARAMETER_USER_ID="userId"

fun Routing.routeUserIdentity() {

    val controller by inject<UserIdentityController>()

    route(ENDPOINT_ACCOUNT) {

        post(ACCOUNT_REGISTER) {
            val userInfo =
                call.receiveNullable<UserIdentityRegisterEntity.DTO>()?.validate() ?: throw IllegalUserIdentityException()
            val userRegisterResult = controller.addNewUserIdentity(userInfo)
            respondWithResult(HttpStatusCode.Created, userRegisterResult).also {
                controller.generateAndSendOTP(userRegisterResult.userId)
            }
        }

        post(ACCOUNT_OTP_GENERATE) {
            val userId = call.receiveParameters()[PARAMETER_USER_ID]?.toIntOrNull()?.validateForNonEmpty() ?: throw IllegalUserIdentityException()
            respondWithResult(HttpStatusCode.OK, controller.generateAndSendOTP(userId))
        }
    }
}