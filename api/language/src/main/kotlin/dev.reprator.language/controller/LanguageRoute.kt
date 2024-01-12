package dev.reprator.language.controller

import dev.reprator.base_ktor.util.respondWithResult
import dev.reprator.language.modal.LanguageEntity.DTO.Companion.mapToModal
import dev.reprator.language.modal.validateLanguageId
import dev.reprator.language.modal.validateLanguageName
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val ENDPOINT_LANGUAGE = "/language"
private const val INPUT_LANGUAGE_ID = "languageId"

fun Routing.routeLanguage() {

    val controller by inject<LanguageController>()

    route(ENDPOINT_LANGUAGE) {
        get {
            respondWithResult(HttpStatusCode.OK, controller.getAllLanguage())
        }

        get("{$INPUT_LANGUAGE_ID}") {
            val languageId = call.parameters[INPUT_LANGUAGE_ID].validateLanguageId()
            respondWithResult(HttpStatusCode.OK, controller.getLanguage(languageId))
        }

        post {
            val languageName: String =
                call.receiveNullable<String>().validateLanguageName()
            respondWithResult(HttpStatusCode.Created, controller.addNewLanguage(languageName))
        }

        patch("{$INPUT_LANGUAGE_ID}") {
            val languageId = call.parameters[INPUT_LANGUAGE_ID].validateLanguageId()
            val languageInfo = call.receiveNullable<Map<String, String>>().mapToModal()

            respondWithResult(HttpStatusCode.OK, controller.editLanguage(languageId, languageInfo))
        }

        delete("{$INPUT_LANGUAGE_ID}") {
            val languageId = call.parameters[INPUT_LANGUAGE_ID].validateLanguageId()
            respondWithResult(HttpStatusCode.OK, controller.deleteLanguage(languageId))
        }
    }
}