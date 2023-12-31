package dev.reprator.userIdentity.controller

import dev.reprator.userIdentity.domain.IllegalUserIdentityException
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal

interface UserIdentityController  {
    @Throws(IllegalUserIdentityException::class)
    suspend fun addNewUserIdentity(userInfo: UserIdentityRegisterEntity): UserIdentityRegisterModal
}

