package dev.reprator.country.domain

import dev.reprator.country.modal.CountryEntity
import dev.reprator.country.modal.CountryId
import dev.reprator.country.modal.CountryModal

interface CountryFacade {

    @Throws(CountryEmptyException::class)
    suspend fun getAllCountry(): Iterable<CountryModal>

    @Throws(CountryNotFoundException::class)
    suspend fun getCountry(id: CountryId): CountryModal

    @Throws(IllegalCountryException::class)
    suspend fun addNewCountry(countryInfo: CountryEntity): CountryModal

    @Throws(IllegalCountryException::class)
    suspend fun editCountry(countryId: CountryId, countryInfo: CountryEntity): Boolean

    @Throws(IllegalCountryException::class)
    suspend fun deleteCountry(id: CountryId)

}