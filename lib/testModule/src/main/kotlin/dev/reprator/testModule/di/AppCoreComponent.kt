package dev.reprator.testModule.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [AppCoreModule::class, NetworkModule::class])
@ComponentScan("dev.reprator.testModule")
class AppCoreComponent