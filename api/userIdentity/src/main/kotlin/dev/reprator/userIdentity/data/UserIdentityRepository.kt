package dev.reprator.userIdentity.data

import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal

interface UserIdentityRepository {

    suspend fun addNewUserIdentity(name: UserIdentityRegisterEntity): UserIdentityRegisterModal

}