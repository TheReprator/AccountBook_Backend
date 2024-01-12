package dev.reprator.language

import dev.reprator.base.mapper.AppMapper
import dev.reprator.language.controller.LanguageController
import dev.reprator.language.controller.LanguageControllerImpl
import dev.reprator.language.data.LanguageRepositoryImpl
import dev.reprator.language.data.LanguageRepository
import dev.reprator.language.domain.LanguageFacadeImpl
import dev.reprator.language.domain.LanguageFacade
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import dev.reprator.language.data.mapper.LanguageResponseMapper
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named

/**
 * The container to inject all dependencies from the language module.
 */
private const val KOIN_NAMED_MAPPER_LANGUAGE = "languageMapper"

val languageModule = module {
    singleOf(::LanguageResponseMapper).withOptions {qualifier = named(KOIN_NAMED_MAPPER_LANGUAGE) } bind AppMapper::class
    single<LanguageRepository> { LanguageRepositoryImpl(get(qualifier = named(KOIN_NAMED_MAPPER_LANGUAGE))) }
    singleOf(::LanguageFacadeImpl) bind LanguageFacade::class
    single { LanguageControllerImpl(get()) } bind LanguageController::class
}