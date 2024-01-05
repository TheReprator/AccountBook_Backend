package dev.reprator.userIdentity.modal

import com.fasterxml.jackson.annotation.JsonTypeInfo
import dev.reprator.country.modal.CountryModal
import dev.reprator.userIdentity.data.USER_CATEGORY
import org.joda.time.DateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = UserIdentityRegisterModal.DTO::class)
interface UserIdentityRegisterModal  {

    val userId: UserIdentityId

    data class DTO (
        override val userId: UserIdentityId
    ) : UserIdentityRegisterModal
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = UserIdentityOTPModal.DTO::class)
interface UserIdentityOTPModal {
    val userId: UserIdentityId
    val phoneNumber: PhoneNumber
    val isPhoneVerified: Boolean
    val id: CountryModal.DTO
    val refreshToken: String
    val userType: USER_CATEGORY

    data class DTO (
        override val userId: UserIdentityId,
        override val phoneNumber: PhoneNumber,
        override val isPhoneVerified: Boolean,
        override val id: CountryModal.DTO,
        override val refreshToken: String,
        override val userType: USER_CATEGORY
    ) : UserIdentityOTPModal
}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = UserIdentityFullModal.DTO::class)
interface UserIdentityFullModal {
    val userId: UserIdentityId
    val country: CountryModal.DTO
    val phoneNumber: PhoneNumber
    val phoneOtp: UserPhoneOTP
    val isPhoneVerified: Boolean
    val userType: USER_CATEGORY
    val refreshToken: String
    val otpCount: Int
    val updateTime: DateTime

    data class DTO (
        override val userId: UserIdentityId,
        override val phoneNumber: PhoneNumber,
        override val isPhoneVerified: Boolean,
        override val country: CountryModal.DTO,
        override val refreshToken: String,
        override val userType: USER_CATEGORY,
        override val phoneOtp: UserPhoneOTP,
        override val otpCount: Int,
        override val updateTime: DateTime
    ) : UserIdentityFullModal
}