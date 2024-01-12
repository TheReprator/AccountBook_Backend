package dev.reprator.base_ktor.api

import dev.reprator.base.beans.APIS
import dev.reprator.base.beans.API_HOST_IDENTIFIER
import dev.reprator.base.usecase.AppResult
import dev.reprator.base.usecase.FailDTOResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*

suspend inline fun <reified T> HttpClient.safeRequest(
    attributes: Attributes,
    apiType: APIS = APIS.INTERNAL_APP,
    block: HttpRequestBuilder.() -> Unit
): AppResult<T> =
    try {
        attributes.put(AttributeKey(API_HOST_IDENTIFIER), apiType)
        val response = request { block() }
        AppResult.Success(response.body())
    } catch (exception: ClientRequestException) {
        println("vikramTest:: ApiError:: ClientRequestException:: "+ exception.response.body())
        AppResult.Error.HttpError(
            code = exception.response.status.value,
            errorBody = exception.response.body(),
            errorMessage = "Status Code: ${exception.response.status.value} - API Key Missing",
        )
    } catch (exception: OwnServerErrorException) {
        println("vikramTest:: ApiError:: OwnServerErrorException:: "+ exception.message)
        AppResult.Error.HttpError(
            code = exception.errorModal.statusCode,
            errorMessage = exception.errorModal.error,
            errorBody = exception.message
        )
    } catch (exception: HttpExceptions) {
        println("vikramTest:: ApiError:: HttpExceptions:: "+ exception.response.body())
        AppResult.Error.HttpError(
            code = exception.response.status.value,
            errorBody = exception.response.body(),
            errorMessage = exception.message,
        )
    } catch (e: Exception) {
        println("vikramTest:: ApiError:: "+ e.message)
        AppResult.Error.GenericError(
            message = e.message,
            errorMessage = "Something went wrong",
        )
    }

class HttpExceptions(
    response: HttpResponse,
    failureReason: String?,
    cachedResponseText: String,
) : ResponseException(response, cachedResponseText) {
    override val message: String = "Status: ${response.status}" + " Failure: $failureReason"
}

class OwnServerErrorException(
    val errorModal: FailDTOResponse,
) : Exception(){
    override val message: String = "Status: ${errorModal.statusCode}" + " Failure: ${errorModal.error}"
}