package dev.reprator

import dev.reprator.base.action.AppDatabaseFactory
import dev.reprator.commonFeatureImpl.di.koinAppCommonDBModule
import dev.reprator.commonFeatureImpl.di.koinAppCommonModule
import dev.reprator.commonFeatureImpl.di.koinAppNetworkClientModule
import dev.reprator.commonFeatureImpl.setupServerPlugin
import dev.reprator.country.controller.routeCountry
import dev.reprator.country.setUpKoinCountry
import dev.reprator.language.controller.routeLanguage
import dev.reprator.language.setUpKoinLanguage
import dev.reprator.splash.controller.routeSplash
import dev.reprator.userIdentity.controller.routeUserIdentity
import io.ktor.server.application.*
import io.ktor.server.netty.*
import dev.reprator.userIdentity.setUpKoinUserIdentityModule
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {

    install(Koin) {
        SLF4JLogger()

        modules(koinAppModule(this@module.environment), koinAppCommonModule(this@module.environment.config),
            koinAppLateInitializationModule(), koinAppDBModule, koinAppCommonDBModule, koinAppNetworkClientModule)

        setUpKoinLanguage()
        setUpKoinCountry()
        setUpKoinUserIdentityModule()
    }


    val dbInstance by inject<AppDatabaseFactory>()
    dbInstance.connect()

    setupServerPlugin()


    routing {
        routeCountry()
        routeLanguage()
        routeSplash()
        routeUserIdentity()
    }
}
