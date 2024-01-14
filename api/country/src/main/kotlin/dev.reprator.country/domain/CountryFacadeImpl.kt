package dev.reprator.country.domain

import dev.reprator.country.data.CountryRepository
import dev.reprator.modals.country.CountryEntity
import dev.reprator.modals.country.CountryId
import dev.reprator.modals.country.CountryModal

class CountryFacadeImpl(private val repository: CountryRepository): CountryFacade {

    override suspend fun getAllCountry(): Iterable<CountryModal> {
        return repository.getAllCountry().ifEmpty {
            throw CountryEmptyException()
        }
    }

    override suspend fun getCountry(id: CountryId): CountryModal {
        return repository.getCountry(id)
    }

    override suspend fun addNewCountry(countryInfo: CountryEntity): CountryModal {
       return repository.addNewCountry(countryInfo)
    }

    override suspend fun editCountry(countryId: CountryId, countryInfo: CountryEntity): Boolean {
        return repository.editCountry(countryId, countryInfo)
    }

    override suspend fun deleteCountry(id: CountryId) {
        return repository.deleteCountry(id)
    }
}