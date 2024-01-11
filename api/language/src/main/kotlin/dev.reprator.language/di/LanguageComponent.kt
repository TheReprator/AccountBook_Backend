package dev.reprator.language.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [LanguageModule::class])
@ComponentScan("dev.reprator.language")
class LanguageComponent