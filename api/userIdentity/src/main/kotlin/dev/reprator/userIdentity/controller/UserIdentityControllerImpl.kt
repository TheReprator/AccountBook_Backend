package dev.reprator.userIdentity.controller

import dev.reprator.userIdentity.domain.UserIdentityFacade
import dev.reprator.userIdentity.modal.*

class UserIdentityControllerImpl(private val userIdentityFacade: UserIdentityFacade) : UserIdentityController {

    override suspend fun addNewUserIdentity(userInfo: UserIdentityRegisterEntity): UserIdentityRegisterModal {
        return userIdentityFacade.addNewUserIdentity(userInfo)
    }

}

