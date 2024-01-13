package dev.reprator.modals.user

import com.fasterxml.jackson.annotation.JsonTypeInfo
import dev.reprator.modals.country.CountryModal

typealias PhoneNumber = String
typealias UserIdentityId = Int

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = UserIdentityOTPModal.DTO::class)
interface UserIdentityOTPModal {
    val userId: UserIdentityId
    val phoneNumber: PhoneNumber
    val isPhoneVerified: Boolean
    val id: CountryModal.DTO
    val refreshToken: String
    val accessToken: String
    val userType: USER_CATEGORY

    data class DTO (
        override val userId: UserIdentityId,
        override val phoneNumber: PhoneNumber,
        override val isPhoneVerified: Boolean,
        override val id: CountryModal.DTO,
        override val refreshToken: String,
        override val accessToken: String,
        override val userType: USER_CATEGORY
    ) : UserIdentityOTPModal
}