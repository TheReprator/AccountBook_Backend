package dev.reprator.userIdentity.controller

import dev.reprator.modals.user.UserIdentityId
import dev.reprator.modals.user.UserIdentityOTPModal
import dev.reprator.userIdentity.domain.IllegalUserIdentityException
import dev.reprator.userIdentity.domain.InvalidTokenException
import dev.reprator.userIdentity.modal.*

interface UserIdentityController  {
    @Throws(IllegalUserIdentityException::class)
    suspend fun addNewUserIdentity(userInfo: UserIdentityRegisterEntity): UserIdentityRegisterModal

    suspend fun generateAndSendOTP(userId: UserIdentityId): Boolean

    @Throws(IllegalUserIdentityException::class)
    suspend fun getUserById(userId: UserIdentityId): UserIdentityFullModal

    @Throws(IllegalUserIdentityException::class)
    suspend fun verifyOtp(userInfo: UserIdentityOtpEntity): UserIdentityOTPModal

    @Throws(InvalidTokenException::class)
    suspend fun refreshToken(accessToken: String): UserIdentityOTPModal

    @Throws(IllegalUserIdentityException::class)
    suspend fun logout(userId: UserIdentityId): Boolean
}

