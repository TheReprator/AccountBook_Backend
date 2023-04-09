package dev.reprator.splash

import dev.reprator.splash.controller.routeSplash
import dev.reprator.testModule.configureCoreModule
import io.ktor.server.application.*
import io.ktor.server.routing.*
import java.io.File

private const val SPLASH_DIRECTORY = "/splashFileDirectory/"

fun Application.module() {

    configureCoreModule()

    routing {
        val directoryUri = Application::class.java.getResource(SPLASH_DIRECTORY)!!.toURI()
        routeSplash(File(directoryUri))
    }
}
