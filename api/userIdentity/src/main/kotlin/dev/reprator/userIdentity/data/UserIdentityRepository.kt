package dev.reprator.userIdentity.data

import dev.reprator.userIdentity.modal.*
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

interface UserIdentityRepository {

    suspend fun addNewUserIdentity(name: UserIdentityRegisterEntity): UserIdentityRegisterModal

    suspend fun getUserById(userId: UserIdentityId): UserIdentityFullModal.DTO

     suspend fun updateUserById(userModal: UserIdentityFullModal, conditionBlock: SqlExpressionBuilder.() -> Op<Boolean> = {
         (TableUserIdentity.id eq userModal.userId)
     }): Int

    suspend fun verifyOtp(userModal: UserIdentityFullModal): UserIdentityOTPModal
}