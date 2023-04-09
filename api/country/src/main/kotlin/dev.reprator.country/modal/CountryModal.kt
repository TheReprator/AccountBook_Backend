package dev.reprator.country.modal

interface CountryModal : CountryEntity {
    val id: CountryId

    data class DTO (
        override val id: CountryId,
        override val name: CountryName,
        override val code: CountryCode,
        override val shortCode: CountryShortCode
    ) : CountryModal
}