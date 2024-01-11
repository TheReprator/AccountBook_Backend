package dev.reprator.di

import io.ktor.server.application.*
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [AppNetworkModule::class])
@ComponentScan("dev.reprator")
class AppComponent(private val applicationEnvironment: ApplicationEnvironment)