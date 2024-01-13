package dev.reprator.userIdentity.data.mapper

import dev.reprator.base.mapper.AppMarkerMapper
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.modals.country.CountryModal
import dev.reprator.modals.user.UserIdentityOTPModal
import dev.reprator.userIdentity.data.TableUserIdentity
import dev.reprator.userIdentity.modal.UserIdentityFullModal
import dev.reprator.userIdentity.modal.UserIdentityRegisterModal
import org.jetbrains.exposed.sql.ResultRow


class UserIdentityResponseRegisterMapper : AppMarkerMapper {

    suspend fun mapToRegisterModal(from: ResultRow): UserIdentityRegisterModal {
        return UserIdentityRegisterModal.DTO(from[TableUserIdentity.id])
    }

    suspend fun mapToOtpModal(from: ResultRow): UserIdentityOTPModal {

        val countryModal = convertToCountryModal(from)

        return UserIdentityOTPModal.DTO(
            from[TableUserIdentity.id],
            from[TableUserIdentity.phoneNumber].toString(),
            from[TableUserIdentity.isPhoneVerified],
            countryModal,
            from[TableUserIdentity.refreshToken] ?: "",
            "",
            from[TableUserIdentity.userType]
        )
    }

    suspend fun mapToOtpModal(from: UserIdentityFullModal): UserIdentityOTPModal.DTO {

        return UserIdentityOTPModal.DTO(
            from.userId,
            from.phoneNumber,
            from.isPhoneVerified,
            from.country,
            from.refreshToken,
            from.accessToken,
            from.userType
        )
    }

    suspend fun mapToFullUserAuthModal(from: ResultRow): UserIdentityFullModal.DTO {

        val countryModal = convertToCountryModal(from)

        return UserIdentityFullModal.DTO(
            from[TableUserIdentity.id], from[TableUserIdentity.phoneNumber].toString(),
            from[TableUserIdentity.isPhoneVerified], countryModal,
            from[TableUserIdentity.refreshToken] ?: "",
            "",
            from[TableUserIdentity.userType],
            from[TableUserIdentity.phoneOtp] ?: 0,
            from[TableUserIdentity.otpCount],
            from[TableUserIdentity.updateTime],
        )
    }

    private fun convertToCountryModal(resultRow: ResultRow): CountryModal.DTO{
        val countryEntity = TableCountryEntity.wrapRow(resultRow)
        return CountryModal.DTO(
            countryEntity.id.value,
            countryEntity.name, countryEntity.isocode, countryEntity.shortcode
        )
    }
}