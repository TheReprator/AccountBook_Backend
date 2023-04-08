package dev.reprator.plugins

import dev.reprator.dao.DAOLanguageFacade
import dev.reprator.dao.DAOLanguageFacadeCacheImpl
import dev.reprator.dao.DAOLanguageFacadeImpl
import dev.reprator.wrapper.AppEmptyException
import dev.reprator.wrapper.FailResponse
import dev.reprator.wrapper.ResultResponse
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.application.*
import java.io.File

fun Application.configureRouting() {

    val dao: DAOLanguageFacade = DAOLanguageFacadeCacheImpl(
        DAOLanguageFacadeImpl(),
        File(environment.config.property("storage.ehcacheFilePath").getString())
    )

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            if(cause is AppEmptyException)
                call.respond(FailResponse(HttpStatusCode.NoContent.value, cause.message))
            else
                call.respond(FailResponse(HttpStatusCode.InternalServerError.value, "500: ${cause.message}"))
        }
    }

    routing {

        route("language") {
            get {
                val languageList = dao.allLanguage()
                if(languageList.isEmpty())
                    throw AppEmptyException()
                else
                    call.respond(ResultResponse(HttpStatusCode.OK.value, languageList))
            }
        }
    }
}
