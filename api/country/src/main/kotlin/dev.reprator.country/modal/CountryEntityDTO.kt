package dev.reprator.country.modal

import dev.reprator.base.usecase.AppEntityValidator
import dev.reprator.country.domain.IllegalCountryException
import dev.reprator.modals.country.*

data class CountryEntityDTO (
    override val name: CountryName,
    override val callingCode: CountryCallingCode,
    override val shortCode: CountryShortCode
) : CountryEntity, AppEntityValidator<CountryEntityDTO>{

    override fun validate(): CountryEntityDTO {
        validateCountryName(name)
        validateCountryShortCode(shortCode)
        callingCode.toString().validateCountryIsoCode()

        return this
    }

    companion object {
        fun Map<String, String>?.mapToModal(): CountryEntityDTO = object: AppEntityValidator<CountryEntityDTO> {

            val data = this@mapToModal ?: throw IllegalCountryException()

            val name:String by data.withDefault { "" }
            val code: String by data.withDefault { "0" }
            val shortCode:String by data.withDefault { "" }

            override fun validate(): CountryEntityDTO {
                if(name.isNotEmpty())
                    validateCountryName(name)

                if(shortCode.isNotEmpty())
                    validateCountryShortCode(shortCode)

                if(0 != code.toInt())
                    code.validateCountryIsoCode()

                return CountryEntityDTO(name, code.toInt(), shortCode)
            }

        }.validate()
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


fun String?.validateCountryIsoCode(): CountryCallingCode {
    val countryCode = this?.toIntOrNull() ?: throw IllegalCountryException()

    if(0 >= countryCode) {
        throw IllegalCountryException()
    }

    return countryCode
}