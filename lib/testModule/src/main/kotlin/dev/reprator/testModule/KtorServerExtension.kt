package dev.reprator.testModule

import dev.reprator.base.beans.APIS
import dev.reprator.base.beans.API_BASE_URL
import dev.reprator.base_ktor.api.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.*
import org.junit.jupiter.api.extension.*
import org.koin.core.Koin

class KtorServerExtension : BeforeEachCallback, AfterEachCallback {

    companion object {
        var TEST_SERVER: NettyApplicationEngine ?= null
    }

    override fun beforeEach(context: ExtensionContext?) {
        val env = applicationEngineEnvironment {
            config = ApplicationConfig("application-test.conf")
            // Public API
            connector {
                host = API_BASE_URL.INTERNAL_APP.value
                port = config.property("ktor.deployment.port").getString().toInt()
            }
        }

        TEST_SERVER = embeddedServer(Netty, env).start(false)
    }

    override fun afterEach(context: ExtensionContext?) {
        TEST_SERVER?.stop(100, 100)
    }
}

suspend inline fun <reified T> hitApiWithClient(
    koin: Koin,
    fullUrl: String ="",
    methodName: HttpMethod = HttpMethod.Post,
    crossinline block: HttpRequestBuilder.() -> Unit ={}
) =
    koin.get<HttpClient>().safeRequest<T>(
        apiType = APIS.INTERNAL_APP,
        attributes = koin.get<Attributes>()
    ) {
        url {
            method = methodName
            path(fullUrl)
        }
        contentType(ContentType.Application.Json)
        block(this@safeRequest)
    }