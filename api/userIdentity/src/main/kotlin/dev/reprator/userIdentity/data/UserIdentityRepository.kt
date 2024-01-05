package dev.reprator.userIdentity.data

import dev.reprator.userIdentity.modal.UserIdentityFullModal
import dev.reprator.userIdentity.modal.UserIdentityId
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal

interface UserIdentityRepository {

    suspend fun addNewUserIdentity(name: UserIdentityRegisterEntity): UserIdentityRegisterModal

    suspend fun getUserById(userId: UserIdentityId): UserIdentityFullModal.DTO

     suspend fun updateUserById(userModal: UserIdentityFullModal)

}