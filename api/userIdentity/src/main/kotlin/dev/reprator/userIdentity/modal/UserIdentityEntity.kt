package dev.reprator.userIdentity.modal

import dev.reprator.core.Validator
import dev.reprator.userIdentity.domain.IllegalUserIdentityException
import java.lang.NumberFormatException

typealias PhoneNumber = String
typealias PhoneCountryCodeId = Int
typealias UserIdentityId = Int
typealias UserPhoneOTP = Int

interface UserIdentityRegisterEntity {
    val phoneNumber: PhoneNumber
    val countryId: PhoneCountryCodeId

    data class DTO (
        override val phoneNumber: PhoneNumber,
        override val countryId: PhoneCountryCodeId
    ) : UserIdentityRegisterEntity, Validator<DTO> {

        override fun validate(): DTO {
            phoneNumber.validatePhoneNumber()
            countryId.validateForNonEmpty()

            return this
        }
    }

    companion object {
        fun Map<String, String>?.mapToModal(): DTO = object: Validator<DTO> {

            val data = this@mapToModal ?: throw IllegalUserIdentityException()

            val phoneNumber: String by data.withDefault { "" }
            val countryId: String by data.withDefault { "" }

            override fun validate(): DTO {
                phoneNumber.validatePhoneNumber()
                countryId.validateForNonEmpty()

                return DTO(phoneNumber.trim(), countryId.trim().toInt())
            }

        }.validate()
    }
}

interface UserIdentityOtpEntity {
    val userId: UserIdentityId
    val phoneOtp: UserPhoneOTP

    data class DTO (
        override val userId: UserIdentityId,
        override val phoneOtp: UserPhoneOTP
    ) : UserIdentityOtpEntity
}


fun PhoneNumber.validatePhoneNumber() {

    if(this.trim().isBlank()) {
        throw IllegalUserIdentityException()
    }

    try {
        this.toBigInteger()
    } catch (e: NumberFormatException){
        throw IllegalUserIdentityException()
    }

    if(this.length !in 7..15) {
        throw IllegalUserIdentityException()
    }
}

fun String.validateForNonEmpty() {
    if(this.trim().isBlank()) {
        throw IllegalUserIdentityException()
    }
}

fun Int.validateForNonEmpty() {
    if(-1 >= this) {
        throw IllegalUserIdentityException()
    }
}