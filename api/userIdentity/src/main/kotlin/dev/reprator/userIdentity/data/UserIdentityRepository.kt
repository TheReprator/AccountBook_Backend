package dev.reprator.userIdentity.data

import dev.reprator.modals.user.UserIdentityId
import dev.reprator.modals.user.UserIdentityOTPModal
import dev.reprator.userIdentity.modal.*
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder

interface UserIdentityRepository {

    suspend fun addNewUserIdentity(name: UserIdentityRegisterEntity): UserIdentityRegisterModal

    suspend fun getUserById(userId: UserIdentityId): UserIdentityFullModal.DTO

     suspend fun updateUserById(userModal: UserIdentityFullModal, conditionBlock: SqlExpressionBuilder.() -> Op<Boolean> = {
         (TableUserIdentity.id eq userModal.userId)
     }): Int

    suspend fun verifyOtp(userModal: UserIdentityFullModal): UserIdentityOTPModal

    suspend fun refreshToken(refreshToken: String, userModal: UserIdentityFullModal): UserIdentityOTPModal.DTO

    suspend fun logout(userId: UserIdentityId): Boolean
}