package dev.reprator.userIdentity.modal

import dev.reprator.country.modal.CountryModal

interface UserIdentityRegisterModal  {

    val userId: UserIdentityId

    data class DTO (
        override val userId: UserIdentityId
    ) : UserIdentityRegisterModal
}

interface UserIdentityOTPModal {
    val userId: UserIdentityId
    val phoneNumber: PhoneNumber
    val isPhoneVerified: Boolean
    val id: CountryModal.DTO
    val refreshToken: String

    data class DTO (
        override val userId: UserIdentityId,
        override val phoneNumber: PhoneNumber,
        override val isPhoneVerified: Boolean,
        override val id: CountryModal.DTO,
        override val refreshToken: String
    ) : UserIdentityOTPModal
}