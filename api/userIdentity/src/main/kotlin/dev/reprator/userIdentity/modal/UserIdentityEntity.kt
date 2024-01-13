package dev.reprator.userIdentity.modal

import dev.reprator.base.beans.LENGTH_OTP
import dev.reprator.base.usecase.AppEntityValidator
import dev.reprator.modals.country.CountryId
import dev.reprator.modals.user.PhoneNumber
import dev.reprator.modals.user.UserIdentityId
import dev.reprator.userIdentity.domain.IllegalUserIdentityException
import java.lang.NumberFormatException

typealias UserPhoneOTP = Int

interface UserIdentityRegisterEntity {
    val phoneNumber: PhoneNumber
    val countryId: CountryId

    data class DTO (
        override val phoneNumber: PhoneNumber,
        override val countryId: CountryId
    ) : UserIdentityRegisterEntity, AppEntityValidator<DTO> {

        override fun validate(): DTO {
            phoneNumber.validatePhoneNumber()
            countryId.validateForNonEmpty()

            return this
        }
    }

    companion object {
        fun Map<String, String>?.mapToModal(): DTO = object: AppEntityValidator<DTO> {

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
    ) : UserIdentityOtpEntity, AppEntityValidator<DTO> {

        override fun validate(): DTO {
            userId.validateForNonEmpty()
            phoneOtp.validateForOtp()

            return this
        }
    }

    companion object {
        fun Map<String, String>?.mapToModal(): DTO = object: AppEntityValidator<DTO> {

            val data = this@mapToModal ?: throw IllegalUserIdentityException()

            val userId: String by data.withDefault { "" }
            val phoneOtp: String by data.withDefault { "" }

            override fun validate(): DTO {
                userId.validateForNonEmpty()
                phoneOtp.validateForNonEmpty()

                return DTO(userId.trim().toInt(), phoneOtp.trim().toInt())
            }

        }.validate()
    }
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

fun Int.validateForNonEmpty(): Int {
    if(-1 >= this) {
        throw IllegalUserIdentityException()
    }

    return this
}

fun Int.validateForOtp(): Boolean {
    if(-1 >= this)
        return false
    val otpLength = this.toString().trim().length
    return LENGTH_OTP == otpLength
}


fun String.safeValidateForEmpty(): Boolean {
    return this.trim().isBlank()
}

fun Int.safeValidateForNonNegative(): Boolean {
    return -1 < this
}

fun Int.safeValidateForOTP(): Boolean {
    if(-1 >= this)
        return false
    val otpLength = this.toString().trim().length
    return LENGTH_OTP == otpLength
}