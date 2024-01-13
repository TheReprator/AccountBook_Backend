package dev.reprator.userIdentity.modal

import com.fasterxml.jackson.annotation.JsonTypeInfo
import dev.reprator.modals.country.CountryModal
import dev.reprator.modals.country.CountryModal.DTO.Companion.emptyCountryModal
import dev.reprator.modals.user.PhoneNumber
import dev.reprator.modals.user.USER_CATEGORY
import dev.reprator.modals.user.UserIdentityId
import org.joda.time.DateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = UserIdentityRegisterModal.DTO::class)
interface UserIdentityRegisterModal  {

    val userId: UserIdentityId

    data class DTO (
        override val userId: UserIdentityId
    ) : UserIdentityRegisterModal
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = UserIdentityFullModal.DTO::class)
interface UserIdentityFullModal {
    val userId: UserIdentityId
    val country: CountryModal.DTO
    val phoneNumber: PhoneNumber
    val phoneOtp: UserPhoneOTP
    val isPhoneVerified: Boolean
    val userType: USER_CATEGORY
    val accessToken: String
    val refreshToken: String
    val otpCount: Int
    val updateTime: DateTime

    data class DTO (
        override val userId: UserIdentityId,
        override val phoneNumber: PhoneNumber,
        override val isPhoneVerified: Boolean,
        override val country: CountryModal.DTO,
        override val refreshToken: String,
        override val accessToken: String = "",
        override val userType: USER_CATEGORY,
        override val phoneOtp: UserPhoneOTP,
        override val otpCount: Int,
        override val updateTime: DateTime
    ) : UserIdentityFullModal {

        companion object {
            fun emptyFullModalByUserId(userId: UserIdentityId): DTO =
                DTO(userId, "", false,
                    emptyCountryModal(), "", "",
                    USER_CATEGORY.admin, -1, -1, DateTime.now().toDateTimeISO())
        }
    }
}