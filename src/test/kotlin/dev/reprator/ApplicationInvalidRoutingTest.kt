package dev.reprator

import dev.reprator.core.ERROR_DESCRIPTION_NOT_FOUND
import dev.reprator.core.FailResponse
import dev.reprator.language.controller.ENDPOINT_LANGUAGE
import dev.reprator.testModule.KtorServerExtension
import dev.reprator.testModule.createHttpClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(KtorServerExtension::class)
internal class ApplicationInvalidRoutingTest {

    @Test
    fun `throw 404, if api doesn't exist`(): Unit = testApplication {
        val client = createHttpClient()
        val response = client.get("${KtorServerExtension.BASE_URL}$ENDPOINT_LANGUAGE/InvalidApi")

        Assertions.assertEquals(response.status, HttpStatusCode.OK)
        val resultBody = response.body<FailResponse>()
        Assertions.assertEquals(resultBody.statusCode, HttpStatusCode.NotFound.value)
        Assertions.assertEquals(ERROR_DESCRIPTION_NOT_FOUND, resultBody.error)
        Assertions.assertNotNull(resultBody)
    }
}