package dev.reprator.testModule

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*

interface MockClientResponseHandler {
    fun handleRequest(
        scope: MockRequestHandleScope,
        request: HttpRequestData
    ): HttpResponseData?
}