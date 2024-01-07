package dev.reprator.testModule

import dev.reprator.core.util.constants.API_BASE_URL
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

class ServerExtension private constructor(private val appPort: Int) : BeforeEachCallback, AfterEachCallback {

    private lateinit var server: NettyApplicationEngine

    companion object {
        fun create(port: Int): ServerExtension {
            return ServerExtension(port)
        }
    }

    override fun beforeEach(context: ExtensionContext?) {
        println("vikram1111:: ServerExtension")
        val env = applicationEngineEnvironment {
            connector {
                host = API_BASE_URL.INTERNAL_APP.value
                port = appPort
                println("vikram::$host:$port")
            }
        }
        server = embeddedServer(Netty, env).start(false)
    }

    override fun afterEach(context: ExtensionContext?) {
        println("vikram1111:: ServerExtension  afterEach")
        if (::server.isInitialized)
            server.stop(100, 100)
    }

}