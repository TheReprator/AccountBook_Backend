package dev.reprator.userIdentity.domain

import dev.reprator.userIdentity.modal.*

interface UserIdentityFacade {

    suspend fun addNewUserIdentity(userInfo: UserIdentityRegisterEntity): UserIdentityRegisterModal

    suspend fun generateAndSendOTP(userId: UserIdentityId): Boolean

    suspend fun getUserById(userId: UserIdentityId): UserIdentityFullModal

    suspend fun verifyOTP(otpInfo: UserIdentityOtpEntity): UserIdentityOTPModal
}