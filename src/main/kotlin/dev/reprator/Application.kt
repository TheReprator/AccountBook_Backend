package dev.reprator

import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.country.setUpKoinCountry
import dev.reprator.language.setUpKoinLanguage
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import dev.reprator.plugins.*
import dev.reprator.userIdentity.setUpKoinUserIdentityModule
import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.Koin
import org.koin.logger.SLF4JLogger

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {

    install(Koin) {
        SLF4JLogger()
        modules(koinAppModule(this@module.environment), koinAppDBModule, koinAppNetworkModule)
        setUpKoinLanguage()
        setUpKoinCountry()
        setUpKoinUserIdentityModule()
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        // header("any header") if you want to add any header
        allowCredentials = true
        allowNonSimpleContentTypes = true
        anyHost()
    }

    get<DatabaseFactory>().connect()

    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = "/shutdown"
        exitCodeSupplier = { 0 }
    }

    configureMonitoring()
    configureContentNegotiation()
    configureRouting()
}
