package dev.reprator.userIdentity.data

import dev.reprator.core.util.dbConfiguration.dbQuery
import dev.reprator.core.util.logger.AppLogger
import dev.reprator.country.data.TableCountry
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.userIdentity.data.mapper.UserIdentityResponseRegisterMapper
import dev.reprator.userIdentity.domain.IllegalUserIdentityException
import dev.reprator.userIdentity.modal.*
import org.jetbrains.exposed.sql.*

class UserIdentityRepositoryImpl(
    private val mapper: UserIdentityResponseRegisterMapper,
    private val appLogger: AppLogger
) : UserIdentityRepository {

    override suspend fun addNewUserIdentity(name: UserIdentityRegisterEntity): UserIdentityRegisterModal = dbQuery {

        val updateOrInsertResult: ResultRow = (TableUserIdentity innerJoin TableCountryEntity.table)
            .select {
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
            .select { TableUserIdentity.id eq userId }
            .map {
                mapper.mapToFullUserAuthModal(it)
            }
            .singleOrNull() ?: throw IllegalUserIdentityException()
    }

    override suspend fun updateUserById(userModal: UserIdentityFullModal) = dbQuery {
        val result =
            TableUserIdentity.update({ TableUserIdentity.id eq userModal.userId }) {
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
    }
}