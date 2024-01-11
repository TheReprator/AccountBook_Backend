package dev.reprator.userIdentity.di

import dev.reprator.core.usecase.JwtTokenService
import dev.reprator.core.util.logger.AppLogger
import dev.reprator.userIdentity.controller.UserIdentityController
import dev.reprator.userIdentity.controller.UserIdentityControllerImpl
import dev.reprator.userIdentity.data.UserIdentityRepository
import dev.reprator.userIdentity.data.UserIdentityRepositoryImpl
import dev.reprator.userIdentity.data.mapper.UserIdentityResponseRegisterMapper
import dev.reprator.userIdentity.domain.UserIdentityFacade
import dev.reprator.userIdentity.domain.UserIdentityFacadeImpl
import dev.reprator.userIdentity.socialVerifier.SMScodeGenerator
import dev.reprator.userIdentity.socialVerifier.SMScodeGeneratorImpl
import io.ktor.client.*
import io.ktor.util.*
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class UserIdentityModule {

    @Factory
    fun provideAppMapper(): UserIdentityResponseRegisterMapper {
        return UserIdentityResponseRegisterMapper()
    }

    @Single
    fun provideUserIdentityRepository(mapper: UserIdentityResponseRegisterMapper, appLogger: AppLogger
    ): UserIdentityRepository {
        return UserIdentityRepositoryImpl(mapper, appLogger)
    }

    @Single
    fun provideSMScodeGenerator(client: HttpClient, attributes: Attributes, appLogger: AppLogger): SMScodeGenerator {
        return SMScodeGeneratorImpl(client, attributes, appLogger)
    }

    @Single
    fun provideUserIdentityFacade(languageRepository: UserIdentityRepository, sMScodeGenerator: SMScodeGenerator, jwtTokenService: JwtTokenService): UserIdentityFacade {
        return UserIdentityFacadeImpl(languageRepository, sMScodeGenerator, jwtTokenService)
    }

    @Single
    fun provideUserIdentityController(languageFacade: UserIdentityFacade): UserIdentityController {
        return UserIdentityControllerImpl(languageFacade)
    }
}