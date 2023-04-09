package dev.reprator.splash.controller

import dev.reprator.core.ResultResponse
import dev.reprator.language.domain.LanguageFacade
import dev.reprator.splash.modal.SplashModal
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import org.koin.ktor.ext.inject
import java.io.File

const val ENDPOINT_SPLASH = "/splash"

fun Routing.routeSplash(splashDirectory: File?) {

    val languageFacade by inject<LanguageFacade>()

    route(ENDPOINT_SPLASH) {
        get {

            val fileAsyncResult = async {
                 splashDirectory?.listFiles()?.map {
                    it.absolutePath
                }.orEmpty()
            }

            val languageAsyncResult = async {
                languageFacade.getAllLanguage().toList()
            }

            val splashModal = SplashModal(fileAsyncResult.await(), languageAsyncResult.await())
            call.respond(ResultResponse(HttpStatusCode.OK.value, splashModal))
        }

    }
}