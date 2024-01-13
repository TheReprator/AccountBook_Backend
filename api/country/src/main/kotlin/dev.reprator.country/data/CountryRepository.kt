package dev.reprator.country.data

import dev.reprator.modals.country.CountryEntity
import dev.reprator.modals.country.CountryId
import dev.reprator.modals.country.CountryModal

interface CountryRepository {

    suspend fun getAllCountry(): List<CountryModal>

    suspend fun getCountry(id: CountryId): CountryModal

    suspend fun addNewCountry(countryInfo: CountryEntity): CountryModal

    suspend fun editCountry(countryId: CountryId, countryInfo: CountryEntity): Boolean

    suspend fun deleteCountry(id: CountryId): Unit

}