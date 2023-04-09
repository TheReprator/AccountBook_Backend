package dev.reprator.country.data

import dev.reprator.country.modal.CountryEntity
import dev.reprator.country.modal.CountryId
import dev.reprator.country.modal.CountryModal

interface CountryRepository {

    suspend fun getAllCountry(): List<CountryModal>

    suspend fun getCountry(id: CountryId): CountryModal

    suspend fun addNewCountry(countryInfo: CountryEntity): CountryModal

    suspend fun editCountry(countryId: CountryId, countryInfo: CountryEntity): Boolean

    suspend fun deleteCountry(id: CountryId): Unit

}