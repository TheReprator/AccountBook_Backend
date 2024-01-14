package dev.reprator.userIdentity.data

import dev.reprator.base.action.AppLogger
import dev.reprator.base_ktor.util.dbConfiguration.dbQuery
import dev.reprator.country.data.TableCountry
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.modals.user.UserIdentityId
import dev.reprator.modals.user.UserIdentityOTPModal
import dev.reprator.userIdentity.data.mapper.UserIdentityResponseRegisterMapper
import dev.reprator.userIdentity.domain.IllegalUserIdentityException
import dev.reprator.userIdentity.domain.InvalidTokenException
import dev.reprator.userIdentity.modal.*
import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime

class UserIdentityRepositoryImpl(
    private val mapper: UserIdentityResponseRegisterMapper,
    private val appLogger: AppLogger
) : UserIdentityRepository {

    override suspend fun addNewUserIdentity(name: UserIdentityRegisterEntity): UserIdentityRegisterModal = dbQuery {

        val updateOrInsertResult: ResultRow = (TableUserIdentity innerJoin TableCountryEntity.table)
            .selectAll().where {
                (TableUserIdentity.phoneNumber eq name.phoneNumber) and
                        (TableCountry.id eq name.countryId)
            }.firstOrNull()
            ?: TableUserIdentity.insert {
                it[phoneNumber] = name.phoneNumber
                it[phoneCountryId] = name.countryId
            }.resultedValues!!.first()

        mapper.mapToRegisterModal(updateOrInsertResult)
    }

    override suspend fun getUserById(userId: UserIdentityId): UserIdentityFullModal.DTO = dbQuery {
        (TableUserIdentity innerJoin TableCountryEntity.table)
            .selectAll().where { TableUserIdentity.id eq userId }
            .map {
                mapper.mapToFullUserAuthModal(it)
            }
            .singleOrNull() ?: throw IllegalUserIdentityException()
    }

    override suspend fun updateUserById(
        userModal: UserIdentityFullModal,
        conditionBlock: SqlExpressionBuilder.() -> Op<Boolean>
    ): Int = dbQuery {
        val result = TableUserIdentity.update({ conditionBlock(this) }) {
            it[updateTime] = userModal.updateTime
            it[isPhoneVerified] = userModal.isPhoneVerified
            if(userModal.otpCount.safeValidateForNonNegative())
                it[otpCount] = userModal.otpCount
            if(userModal.phoneOtp.safeValidateForOTP())
                it[phoneOtp] = userModal.phoneOtp
            if(!userModal.refreshToken.safeValidateForEmpty())
                it[refreshToken] = userModal.refreshToken
            //it[userType] = userModal.userType
        }

        appLogger.e { "record updated status:: $result" }
        result
    }

    override suspend fun verifyOtp(userModal: UserIdentityFullModal): UserIdentityOTPModal {
        val result = updateUserById(userModal) {
            (TableUserIdentity.id eq userModal.userId) and (TableUserIdentity.phoneOtp eq userModal.phoneOtp)
        }

        if(0 < result) {
            return mapper.mapToOtpModal(userModal)
        }
        throw IllegalUserIdentityException()
    }

    override suspend fun refreshToken(refreshToken: String, userModal: UserIdentityFullModal): UserIdentityOTPModal.DTO {
        val result = updateUserById(userModal) {
            (TableUserIdentity.id eq userModal.userId) and (TableUserIdentity.refreshToken eq refreshToken)
        }

        if(0 < result) {
            return mapper.mapToOtpModal(userModal)
        }
        throw InvalidTokenException()
    }

    override suspend fun logout(userId: UserIdentityId): Boolean {
        val updateCount = dbQuery {
             TableUserIdentity.update({ TableUserIdentity.id eq userId }) {
                it[updateTime] = DateTime.now().toDateTimeISO()
                it[refreshToken] = ""
                it[otpCount] = 0
                it[phoneOtp] = -1
                it[isPhoneVerified] = false
            }
        }
        return 0 < updateCount
    }
}