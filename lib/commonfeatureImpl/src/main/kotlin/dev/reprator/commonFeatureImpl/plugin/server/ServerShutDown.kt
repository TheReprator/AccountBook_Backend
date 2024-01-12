package dev.reprator.commonFeatureImpl.plugin.server

import dev.reprator.base.action.AppDatabaseFactory

import io.ktor.server.application.*
import io.ktor.server.engine.*
import org.koin.ktor.ext.inject

fun Application.configureServerShutDown() {


    install(ShutDownUrl.ApplicationCallPlugin) {

        val dbInstance by this@configureServerShutDown.inject<AppDatabaseFactory>()
        dbInstance.close()

        shutDownUrl = "/shutdown"
        exitCodeSupplier = { 0 }
    }

}