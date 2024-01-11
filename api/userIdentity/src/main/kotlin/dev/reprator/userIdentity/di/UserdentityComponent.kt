package dev.reprator.userIdentity.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [UserIdentityModule::class])
@ComponentScan("dev.reprator.identity")
class UserdentityComponent