package dev.reprator.country.controller

import dev.reprator.country.domain.CountryEmptyException
import dev.reprator.country.domain.CountryNotFoundException
import dev.reprator.country.domain.IllegalCountryException
import dev.reprator.modals.country.*

interface CountryController  {

    @Throws(CountryEmptyException::class)
    suspend fun getAllCountry(): Iterable<CountryModal>

    @Throws(CountryNotFoundException::class)
    suspend fun getCountry(id: CountryId): CountryModal

    @Throws(IllegalCountryException::class)
    suspend fun addNewCountry(countryInfo: CountryEntity): CountryModal

    @Throws(IllegalCountryException::class)
    suspend fun editCountry(id: CountryId, countryInfo: CountryEntity): Boolean

    @Throws(IllegalCountryException::class)
    suspend fun deleteCountry(id: CountryId)
}

