package dev.reprator.commonFeatureImpl.plugin.server

import dev.reprator.commonFeatureImpl.di.KEY_SERVICE_SHUTDOWN
import io.ktor.server.application.*
import io.ktor.server.engine.*
import org.koin.core.qualifier.named
import org.koin.ktor.ext.get

fun Application.configureServerShutDown() {

    install(ShutDownUrl.ApplicationCallPlugin) {
        shutDownUrl = this@configureServerShutDown.get<String>(named(KEY_SERVICE_SHUTDOWN))
        exitCodeSupplier = { 0 }
    }

}