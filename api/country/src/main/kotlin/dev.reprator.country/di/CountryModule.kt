package dev.reprator.country.di

import dev.reprator.core.util.AppMapper
import dev.reprator.country.controller.CountryController
import dev.reprator.country.controller.CountryControllerImpl
import dev.reprator.country.data.CountryRepository
import dev.reprator.country.data.CountryRepositoryImpl
import dev.reprator.country.data.TableCountryEntity
import dev.reprator.country.data.mapper.CountryResponseMapper
import dev.reprator.country.domain.CountryFacade
import dev.reprator.country.domain.CountryFacadeImpl
import dev.reprator.country.modal.CountryModal
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

const val KOIN_NAMED_MAPPER_COUNTRY = "countryMapper"

@Module
class CountryModule {

    @Factory
    @Named(KOIN_NAMED_MAPPER_COUNTRY)
    fun provideAppMapper(): AppMapper<TableCountryEntity, CountryModal> {
        return CountryResponseMapper()
    }

    @Single
    fun provideCountryRepository(mapper: AppMapper<TableCountryEntity, CountryModal>): CountryRepository {
        return CountryRepositoryImpl(mapper)
    }

    @Single
    fun provideCountryFacade(countryRepository: CountryRepository): CountryFacade {
        return CountryFacadeImpl(countryRepository)
    }

    @Single
    fun provideCountryController(countryFacade: CountryFacade): CountryController {
        return CountryControllerImpl(countryFacade)
    }
}