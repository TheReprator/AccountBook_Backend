package dev.reprator.core.util.api

import com.fasterxml.jackson.databind.JsonMappingException
import dev.reprator.core.util.constants.APIS
import dev.reprator.core.util.constants.API_HOST_IDENTIFIER
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.util.*

suspend inline fun <reified T> HttpClient.safeRequest(
    attributes: Attributes,
    apiType: APIS = APIS.INTERNAL_APP,
    block: HttpRequestBuilder.() -> Unit
): ApiResponse<T> =
    try {
        attributes.put(AttributeKey(API_HOST_IDENTIFIER), apiType)
        val response = request { block() }
        ApiResponse.Success(response.body())
    } catch (exception: ClientRequestException) {
        ApiResponse.Error.HttpError(
            code = exception.response.status.value,
            errorBody = exception.response.body(),
            errorMessage = "Status Code: ${exception.response.status.value} - API Key Missing",
        )
    } catch (exception: HttpExceptions) {
        ApiResponse.Error.HttpError(
            code = exception.response.status.value,
            errorBody = exception.response.body(),
            errorMessage = exception.message,
        )
    } catch (e: JsonMappingException) {
        ApiResponse.Error.SerializationError(
            message = e.message,
            errorMessage = "Something went wrong, parsing error",
        )
    } catch (e: Exception) {
        e.stackTrace
        println("vikramTest"+ e.stackTrace)
        println("vikramTest11"+ e.localizedMessage)
        println("vikramTest12"+ e.message)
        ApiResponse.Error.GenericError(
            message = e.message,
            errorMessage = "Something went wrong",
        )
    }