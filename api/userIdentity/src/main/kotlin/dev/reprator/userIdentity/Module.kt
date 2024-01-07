package dev.reprator.userIdentity

import dev.reprator.userIdentity.controller.UserIdentityController
import dev.reprator.userIdentity.controller.UserIdentityControllerImpl
import dev.reprator.userIdentity.data.UserIdentityRepositoryImpl
import dev.reprator.userIdentity.data.UserIdentityRepository
import dev.reprator.userIdentity.domain.UserIdentityFacadeImpl
import dev.reprator.userIdentity.domain.UserIdentityFacade
import org.koin.dsl.module
import dev.reprator.userIdentity.data.mapper.UserIdentityResponseRegisterMapper
import dev.reprator.userIdentity.socialVerifier.SMScodeGenerator
import dev.reprator.userIdentity.socialVerifier.SMScodeGeneratorImpl

val userIdentityModule = module {
    factory<UserIdentityResponseRegisterMapper> { UserIdentityResponseRegisterMapper() }
    single<UserIdentityRepository> { UserIdentityRepositoryImpl(get(), get()) }

    single<SMScodeGenerator> { SMScodeGeneratorImpl(get(), get(), get()) }
    single<UserIdentityFacade> { UserIdentityFacadeImpl(get(), get(), get()) }

    single<UserIdentityController> { UserIdentityControllerImpl(get()) }
}