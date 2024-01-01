package dev.reprator.userIdentity.domain

import dev.reprator.userIdentity.modal.*

interface UserIdentityFacade {

    suspend fun addNewUserIdentity(userInfo: UserIdentityRegisterEntity): UserIdentityRegisterModal

    suspend fun generateAndSendOTP(userId: UserIdentityId): Boolean
}