package dev.reprator.language.di

import dev.reprator.core.util.AppMapper
import dev.reprator.language.controller.LanguageController
import dev.reprator.language.controller.LanguageControllerImpl
import dev.reprator.language.data.LanguageRepository
import dev.reprator.language.data.LanguageRepositoryImpl
import dev.reprator.language.data.mapper.LanguageResponseMapper
import dev.reprator.language.domain.LanguageFacade
import dev.reprator.language.domain.LanguageFacadeImpl
import dev.reprator.language.modal.LanguageModal
import org.jetbrains.exposed.sql.ResultRow
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

const val KOIN_NAMED_MAPPER_LANGUAGE = "languageMapper"

@Module
class LanguageModule {

    @Factory
    @Named(KOIN_NAMED_MAPPER_LANGUAGE)
    fun provideAppMapper(): AppMapper<ResultRow, LanguageModal> {
        return LanguageResponseMapper()
    }

    @Single
    fun provideLanguageRepository(mapper: AppMapper<ResultRow, LanguageModal>): LanguageRepository {
        return LanguageRepositoryImpl(mapper)
    }

    @Single
    fun provideLanguageFacade(languageRepository: LanguageRepository): LanguageFacade {
        return LanguageFacadeImpl(languageRepository)
    }

    @Single
    fun provideLanguageController(languageFacade: LanguageFacade): LanguageController {
        return LanguageControllerImpl(languageFacade)
    }
}