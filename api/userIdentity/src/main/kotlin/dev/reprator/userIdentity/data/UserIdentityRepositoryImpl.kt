package dev.reprator.userIdentity.data

import dev.reprator.core.AppLogger
import dev.reprator.core.Mapper
import dev.reprator.core.dbQuery
import dev.reprator.country.data.TableCountry
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import dev.reprator.userIdentity.socialVerifier.SMScodeGenerator
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.*

class UserIdentityRepositoryImpl(
    private val appLogger: AppLogger,
    private val smsCodeGenerator: SMScodeGenerator,
    private val mapper: Mapper<ResultRow, UserIdentityRegisterModal>
) : UserIdentityRepository {

    private suspend fun resultRowToUserIdentity(row: ResultRow): UserIdentityRegisterModal = mapper.map(row)


    override suspend fun addNewUserIdentity(name: UserIdentityRegisterEntity): UserIdentityRegisterModal = dbQuery {

        val userExistQuery = (TableUserIdentity innerJoin TableCountryEntity.table)
            .select {
                (TableUserIdentity.phoneNumber eq name.phoneNumber) and
                        (TableCountry.id eq name.countryId)
            }.firstOrNull()


        val otpCode = smsCodeGenerator.generateCode()

        val updateOrInsertResult: ResultRow = if (null == userExistQuery) {
            TableUserIdentity.insert {
                it[phoneNumber] = name.phoneNumber
                it[phoneCountryId] = name.countryId
                it[phoneOtp] = otpCode
            }.resultedValues!!.first()

        } else {
            TableUserIdentity.update({ TableUserIdentity.id eq userExistQuery[TableUserIdentity.id] }) {
                it[phoneOtp] = otpCode
            }
            userExistQuery
        }

        val countryCallingCode = TableCountry.select {
            TableCountry.id eq name.countryId
        }.limit(1).map {
            it[TableCountry.callingCode]
        }.first()

        appLogger.e { "vikramResponseTest00:: ${updateOrInsertResult[TableUserIdentity.otpCount]}" }
        val smsSendResult = smsCodeGenerator.sendTokenToMobileNumber(countryCallingCode, name.phoneNumber, otpCode)
        if(smsSendResult) {
            val isOTPCountUpdated = TableUserIdentity.update({ TableUserIdentity.id eq updateOrInsertResult[TableUserIdentity.id] }) {
                it[otpCount] = updateOrInsertResult[otpCount] + 1
            } > 0
            appLogger.e { "vikramResponseTest11:: $isOTPCountUpdated" }
        }

        appLogger.e { "vikramResponseTest12:: $smsSendResult" }
        resultRowToUserIdentity(updateOrInsertResult)
    }
}