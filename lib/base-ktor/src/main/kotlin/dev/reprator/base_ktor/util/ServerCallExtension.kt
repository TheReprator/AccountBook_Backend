package dev.reprator.base_ktor.util

import dev.reprator.base.beans.AppMultipartDTO
import dev.reprator.base.usecase.FailDTOResponse
import dev.reprator.base.usecase.ResultDTOResponse
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

/*https://github.com/TheChance101/beep-beep/blob/develop/api_gateway/src/main/kotlin/org/thechance/api_gateway/endpoints/utils/Extentions.kt*/
suspend inline fun <reified T> PipelineContext<Unit, ApplicationCall>.respondWithResult(
    statusCode: HttpStatusCode, result: T, message: String? = null
) {
    call.respond(statusCode, ResultDTOResponse(statusCode.value, result))
}

suspend fun respondWithError(
    call: ApplicationCall, statusCode: HttpStatusCode, errorMessage: Map<Int, String>? = null
) {
    call.respond(statusCode, FailDTOResponse(statusCode.value, errorMessage.toString()))
}

suspend inline fun <reified T> PipelineContext<Unit, ApplicationCall>.receiveMultipart(
    imageValidator: ImageValidator
): AppMultipartDTO<T> {

    val multipart = call.receiveMultipart()
    var fileBytes: ByteArray? = null
    var data: T? = null

    multipart.forEachPart { part ->
        when (part) {
            is PartData.FileItem -> {
                if (imageValidator.isValid(part.originalFileName)) {
                    fileBytes = part.streamProvider().readBytes()
                }
            }

            is PartData.FormItem -> {
                if (part.name == "data") {
                    val json = part.value.trimIndent()
                   // data = Json.decodeFromString<T>(json)
                    throw Exception("to be handled or decored by jackson")
                }
            }

            else -> {}
        }
        part.dispose()
    }
    return AppMultipartDTO(data = data!!, image = fileBytes)
}

class ImageValidator {
    fun isValid(name: String?) : Boolean {
        val extension = name?.substringAfterLast(".", "")
        val isImage = extension?.let { it in listOf("jpg", "jpeg", "png") } ?: false
        return if (isImage) true else throw Exception("Invalid File")
    }
}