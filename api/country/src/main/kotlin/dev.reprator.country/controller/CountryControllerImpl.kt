package dev.reprator.country.controller

import dev.reprator.country.domain.CountryFacade
import dev.reprator.country.modal.CountryEntity
import dev.reprator.country.modal.CountryId
import dev.reprator.country.modal.CountryModal

class CountryControllerImpl(private val countryFacade: CountryFacade) : CountryController {

    override suspend fun getAllCountry(): Iterable<CountryModal> = countryFacade.getAllCountry()

    override suspend fun getCountry(id: CountryId) = countryFacade.getCountry(id)

    override suspend fun addNewCountry(countryInfo: CountryEntity) = countryFacade.addNewCountry(countryInfo)

    override suspend fun editCountry(id: CountryId, countryInfo: CountryEntity): Boolean = countryFacade.editCountry(id, countryInfo)

    override suspend fun deleteCountry(id: CountryId) = countryFacade.deleteCountry(id)

}

