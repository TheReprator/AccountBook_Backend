package dev.reprator.userIdentity.domain

import dev.reprator.modals.user.UserIdentityId
import dev.reprator.modals.user.UserIdentityOTPModal
import dev.reprator.userIdentity.modal.*

interface UserIdentityFacade {

    suspend fun addNewUserIdentity(userInfo: UserIdentityRegisterEntity): UserIdentityRegisterModal

    suspend fun generateAndSendOTP(userId: UserIdentityId): Boolean

    suspend fun getUserById(userId: UserIdentityId): UserIdentityFullModal

    suspend fun verifyOTP(otpInfo: UserIdentityOtpEntity): UserIdentityOTPModal

    @Throws(InvalidTokenException::class)
    suspend fun refreshToken(accessToken: String): UserIdentityOTPModal.DTO

    suspend fun logout(userId: UserIdentityId): Boolean
}