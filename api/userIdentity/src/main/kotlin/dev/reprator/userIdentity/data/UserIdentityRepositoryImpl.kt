package dev.reprator.userIdentity.data

import dev.reprator.core.Mapper
import dev.reprator.core.dbQuery
import dev.reprator.country.data.TableCountry
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterEntity
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.*

class UserIdentityRepositoryImpl(private val smsCodeGenerator: SMScodeGenerator, 
                                 private val mapper: Mapper<ResultRow, UserIdentityRegisterModal>) : UserIdentityRepository {

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

        resultRowToUserIdentity(updateOrInsertResult)
    }
}