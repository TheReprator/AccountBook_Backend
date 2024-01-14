package dev.reprator.userIdentity.socialVerifier

import dev.reprator.testModule.clientUtility.MockClientResponseHandler
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class SmsResponseHandler : MockClientResponseHandler {
    override fun handleRequest(
        scope: MockRequestHandleScope,
        request: HttpRequestData
    ): HttpResponseData? {

        if (request.url.encodedPath.contains(VERIFY_SMS).not()) {
            return null
        }

        val (statusCode, responseContent) = if (!(request.headers.contains("API-Key") && request.headers.contains("User-ID"))) {
            HttpStatusCode.Forbidden to MockedApiResponseSMS.FOR_INVALID
        } else {
            val formData = (request.body as FormDataContent).formData
            val numberLength = formData["number"]?.length ?: 0
            val responseContent = if (10 < numberLength)
                MockedApiResponseSMS.FOR_VALID
            else
                MockedApiResponseSMS.FOR_INVALID

            val statusCode = if (10 < numberLength)
                HttpStatusCode.OK
            else
                HttpStatusCode.BadRequest

            statusCode to responseContent
        }
        return scope.respond(
            content = responseContent,
            status = statusCode,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }
}

internal fun MockRequestHandleScope.errorResponse(): HttpResponseData {
    return respond(
        content = "",
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}

object MockedApiResponseSMS {
    const val FOR_INVALID = """
{
    "number-valid": false,
    "security-code": "",
    "sent": "false"
}
"""
    const val FOR_VALID = """
{
    "number-valid": true,
    "security-code": "1234",
    "sent": "true"
}
"""
}