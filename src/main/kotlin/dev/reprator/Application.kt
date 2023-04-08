package dev.reprator

import dev.reprator.dao.DatabaseFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import dev.reprator.plugins.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init(environment.config)
    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = "/shutdown"
        exitCodeSupplier = { 0 }
    }
    configureMonitoring()
    configureSerialization()
    configureRouting()
}
