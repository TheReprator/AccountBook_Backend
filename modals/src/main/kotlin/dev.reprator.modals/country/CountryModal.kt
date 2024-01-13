package dev.reprator.modals.country

interface CountryModal : CountryEntity {
    val id: CountryId

    data class DTO (
        override val id: CountryId,
        override val name: CountryName,
        override val callingCode: CountryCallingCode,
        override val shortCode: CountryShortCode
    ) : CountryModal {
        companion object {
            fun emptyCountryModal(): DTO = DTO(-1, "", -1, "" )
        }
    }
}