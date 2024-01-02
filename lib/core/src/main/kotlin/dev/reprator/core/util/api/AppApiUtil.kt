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
        println("vikramTest:: ApiError:: ClientRequestException:: "+ exception.response.body())
        ApiResponse.Error.HttpError(
            code = exception.response.status.value,
            errorBody = exception.response.body(),
            errorMessage = "Status Code: ${exception.response.status.value} - API Key Missing",
        )
    } catch (exception: HttpExceptions) {
        println("vikramTest:: ApiError:: HttpExceptions:: "+ exception.response.body())
        ApiResponse.Error.HttpError(
            code = exception.response.status.value,
            errorBody = exception.response.body(),
            errorMessage = exception.message,
        )
    } catch (e: JsonMappingException) {
        println("vikramTest:: ApiError:: JsonMappingException:: "+ e.message)
        ApiResponse.Error.SerializationError(
            message = e.message,
            errorMessage = "Something went wrong, parsing error",
        )
    } catch (e: Exception) {
        println("vikramTest:: ApiError:: "+ e.message)
        ApiResponse.Error.GenericError(
            message = e.message,
            errorMessage = "Something went wrong",
        )
    }