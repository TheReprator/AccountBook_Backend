package dev.reprator.testModule

import dev.reprator.core.util.constants.API_BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import org.junit.jupiter.api.extension.*

class KtorServerExtension : BeforeEachCallback, AfterEachCallback {


    companion object {

        var TEST_SERVER: NettyApplicationEngine ?= null

        private var testPort: Int = 0

        val TEST_BASE_URL: String
            get() = "http://${API_BASE_URL.INTERNAL_APP.value}:$testPort"
    }

    override fun beforeEach(context: ExtensionContext?) {
        val env = applicationEngineEnvironment {
            config = ApplicationConfig("application-test.conf")
            // Public API
            connector {
                host = API_BASE_URL.INTERNAL_APP.value
                port = config.property("ktor.deployment.port").getString().toInt()
                testPort = port
            }
        }

        TEST_SERVER = embeddedServer(Netty, env).start(false)
    }

    override fun afterEach(context: ExtensionContext?) {
            TEST_SERVER?.stop(100, 100)
    }
}

fun createHttpClient(): HttpClient {
    val client = HttpClient {

        install(ContentNegotiation) {
            jackson()
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
            requestTimeoutMillis = 30000
        }
    }

    return client
}