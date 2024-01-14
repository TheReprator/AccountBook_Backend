package dev.reprator.country

import dev.reprator.base.mapper.AppMapper
import dev.reprator.country.controller.CountryController
import dev.reprator.country.controller.CountryControllerImpl
import dev.reprator.country.data.CountryRepository
import dev.reprator.country.data.CountryRepositoryImpl
import dev.reprator.country.data.mapper.CountryResponseMapper
import dev.reprator.country.domain.CountryFacade
import dev.reprator.country.domain.CountryFacadeImpl
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * The container to inject all dependencies from the language module.
 */
private const val KOIN_NAMED_MAPPER = "countryMapper"

val countryModule = module {
    single(named(KOIN_NAMED_MAPPER)) { CountryResponseMapper() } bind AppMapper::class
    single<CountryRepository> { CountryRepositoryImpl(get(named(KOIN_NAMED_MAPPER))) }
    singleOf(::CountryFacadeImpl) bind CountryFacade::class
    single { CountryControllerImpl(get()) } bind CountryController::class
}