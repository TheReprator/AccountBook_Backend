package dev.reprator.userIdentity.controller

import dev.reprator.core.util.respondWithResult
import dev.reprator.userIdentity.domain.IllegalUserIdentityException
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val ENDPOINT_ACCOUNT = "/accounts"
const val ACCOUNT_REGISTER = "register"
const val ACCOUNT_OTP_VERIFY = "otpVerify"

fun Routing.routeUserIdentity() {

    val controller by inject<UserIdentityController>()

    route(ENDPOINT_ACCOUNT) {

        post(ACCOUNT_REGISTER) {
            val userInfo =
                call.receiveNullable<UserIdentityRegisterEntity.DTO>()?.validate() ?: throw IllegalUserIdentityException()
            respondWithResult(HttpStatusCode.Created, controller.addNewUserIdentity(userInfo))
        }

//        post(ACCOUNT_OTP_VERIFY) {
//            val userInfo =
//                call.receiveNullable<UserIdentityOtpEntity.DTO>()?.validate() ?: throw IllegalUserIdentityException()
//            call.respond(ResultResponse(HttpStatusCode.Created.value, controller.addNewUserIdentity(userInfo)))
//        }
    }
}