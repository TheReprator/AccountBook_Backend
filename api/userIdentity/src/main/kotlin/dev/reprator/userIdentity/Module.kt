package dev.reprator.userIdentity

import dev.reprator.core.Mapper
import dev.reprator.userIdentity.controller.UserIdentityController
import dev.reprator.userIdentity.controller.UserIdentityControllerImpl
import dev.reprator.userIdentity.data.SMScodeGenerator
import dev.reprator.userIdentity.data.UserIdentityRepositoryImpl
import dev.reprator.userIdentity.data.UserIdentityRepository
import dev.reprator.userIdentity.domain.UserIdentityFacadeImpl
import dev.reprator.userIdentity.domain.UserIdentityFacade
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import dev.reprator.userIdentity.data.mapper.UserIdentityResponseRegisterMapper
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named

/**
 * The container to inject all dependencies from the UserIdentity module.
 */
private const val KOIN_NAMED_MAPPER = "userIdentityMapper"

val userIdentityModule = module {
    singleOf(::UserIdentityResponseRegisterMapper).withOptions {qualifier = named(KOIN_NAMED_MAPPER) } bind Mapper::class
    singleOf(::SMScodeGenerator) bind SMScodeGenerator::class
    single<UserIdentityRepository> { UserIdentityRepositoryImpl(get(), get(qualifier = named(KOIN_NAMED_MAPPER))) }
    singleOf(::UserIdentityFacadeImpl) bind UserIdentityFacade::class
    single { UserIdentityControllerImpl(get()) } bind UserIdentityController::class
}