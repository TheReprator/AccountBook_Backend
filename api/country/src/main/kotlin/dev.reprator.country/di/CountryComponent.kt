package dev.reprator.country.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [CountryModule::class])
@ComponentScan("dev.reprator.country")
class CountryComponent