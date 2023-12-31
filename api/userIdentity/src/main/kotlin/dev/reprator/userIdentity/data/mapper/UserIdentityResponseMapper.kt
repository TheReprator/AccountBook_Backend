package dev.reprator.userIdentity.data.mapper

import dev.reprator.core.Mapper
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.country.modal.CountryModal
import dev.reprator.userIdentity.data.TableUserIdentity
import dev.reprator.userIdentity.modal.UserIdentityOTPModal
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import org.jetbrains.exposed.sql.ResultRow


class UserIdentityResponseRegisterMapper : Mapper<ResultRow, UserIdentityRegisterModal> {
    override suspend fun map(from: ResultRow): UserIdentityRegisterModal {
        return UserIdentityRegisterModal.DTO(from[TableUserIdentity.id])
    }
}

class UserIdentityResponseOTPMapper : Mapper<ResultRow, UserIdentityOTPModal> {

    override suspend fun map(from: ResultRow): UserIdentityOTPModal {

        val countryEntity = TableCountryEntity.wrapRow(from)
        val countryModal = CountryModal.DTO(
            countryEntity.id.value,
            countryEntity.name, countryEntity.isocode, countryEntity.shortcode
        )

        return UserIdentityOTPModal.DTO(
            from[TableUserIdentity.id], from[TableUserIdentity.phoneNumber].toString(),
            from[TableUserIdentity.isPhoneVerified], countryModal,
            from[TableUserIdentity.refreshToken] ?: "",
            from[TableUserIdentity.userType]
        )
    }
}