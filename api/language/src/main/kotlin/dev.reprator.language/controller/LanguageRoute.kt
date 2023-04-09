package dev.reprator.language.controller

import dev.reprator.core.ResultResponse
import dev.reprator.core.exception.StatusCodeException
import dev.reprator.language.modal.LanguageEntity
import dev.reprator.language.modal.LanguageEntity.DTO.Companion.mapToModal
import dev.reprator.language.modal.validateLanguageId
import dev.reprator.language.modal.validateLanguageName
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

const val ENDPOINT_LANGUAGE = "/language"
private const val INPUT_LANGUAGE_ID = "languageId"

fun Routing.routeLanguage() {

    val controller by inject<LanguageController>()

    route(ENDPOINT_LANGUAGE) {
        get {
            call.respond(ResultResponse(HttpStatusCode.OK.value, controller.getAllLanguage()))
        }

        get("{$INPUT_LANGUAGE_ID}") {
            val languageId = call.parameters[INPUT_LANGUAGE_ID].validateLanguageId()
            call.respond(ResultResponse(HttpStatusCode.OK.value, controller.getLanguage(languageId)))
        }

        post {
            val languageName: String =
                call.receiveNullable<String>().validateLanguageName()
            call.respond(ResultResponse(HttpStatusCode.Created.value, controller.addNewLanguage(languageName)))
        }

        patch("{$INPUT_LANGUAGE_ID}") {
            val languageId = call.parameters[INPUT_LANGUAGE_ID].validateLanguageId()
            val languageInfo = call.receiveNullable<Map<String, String>>().mapToModal()

            call.respond(ResultResponse(HttpStatusCode.OK.value, controller.editLanguage(languageId, languageInfo)))
        }

        delete("{$INPUT_LANGUAGE_ID}") {
            val languageId = call.parameters[INPUT_LANGUAGE_ID].validateLanguageId()
            call.respond(ResultResponse(HttpStatusCode.OK.value, controller.deleteLanguage(languageId)))
        }
    }
}