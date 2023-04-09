package dev.reprator.country.modal

import dev.reprator.core.Validator
import dev.reprator.country.domain.IllegalCountryException

typealias CountryId = Int
typealias CountryName = String
typealias CountryCode = Int
typealias CountryShortCode = String

interface CountryEntity {
    val name: CountryName
    val code: CountryCode
    val shortCode: CountryShortCode

    companion object {
        fun Map<String, String>?.mapToModal(): DTO = object: Validator<DTO> {

            val data = this@mapToModal ?: throw IllegalCountryException()

            val name:String by data.withDefault { "" }
            val code: String by data.withDefault { "0" }
            val shortCode:String by data.withDefault { "" }

            override fun validate(): DTO {
                if(name.isNotEmpty())
                    validateCountryName(name)

                if(shortCode.isNotEmpty())
                    validateCountryShortCode(shortCode)

                if(0 != code.toInt())
                    code.validateCountryIsoCode()

                return DTO(name, code.toInt(), shortCode)
            }

        }.validate()
    }

    data class DTO (
        override val name: CountryName,
        override val code: CountryCode,
        override val shortCode: CountryShortCode
    ) : CountryEntity, Validator<DTO> {

        override fun validate(): DTO {
            validateCountryName(name)
            validateCountryShortCode(shortCode)
            code.toString().validateCountryIsoCode()

            return this
        }
    }
}

fun validateCountryName(countryName: CountryName) {
    if(countryName.isBlank()) {
        throw IllegalCountryException()
    }

    if(3 >= countryName.length) {
        throw IllegalCountryException()
    }
}

fun validateCountryShortCode(countryShortCode: CountryShortCode) {
    if(countryShortCode.isBlank()) {
        throw IllegalCountryException()
    }
}

fun String?.validateCountryIsoCode(): CountryCode {
    val countryCode = this?.toIntOrNull() ?: throw IllegalCountryException()

    if(0 >= countryCode) {
        throw IllegalCountryException()
    }

    return countryCode
}