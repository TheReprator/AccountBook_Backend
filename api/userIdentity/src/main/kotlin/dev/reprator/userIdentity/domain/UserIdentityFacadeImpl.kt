package dev.reprator.userIdentity.domain

import dev.reprator.userIdentity.data.UserIdentityRepository
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal

class UserIdentityFacadeImpl(private val repository: UserIdentityRepository): UserIdentityFacade {

    override suspend fun addNewUserIdentity(userInfo: UserIdentityRegisterEntity): UserIdentityRegisterModal {
        return repository.addNewUserIdentity(userInfo)
    }
}