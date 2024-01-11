package dev.reprator.testModule.di

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.reprator.core.util.logger.AppLogger
import dev.reprator.testModule.Types
import dev.reprator.testModule.plugin.*
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.server.config.*
import io.ktor.util.*
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class NetworkModule {

    @Single
    fun provideServerPort(): Int {
        return ApplicationConfig("application-test.conf").property("ktor.deployment.port").getString().toInt()
    }

    @Single
    fun provideHttpClientAttribute(): Attributes {
        return Attributes(true)
    }

    @Single
    fun provideJacksonObjectMapper(): ObjectMapper =
        jacksonObjectMapper().apply {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            })
        }

    @Single
    fun provideHttpClientEngine(): HttpClientEngine {
        return CIO.create()
    }

    @Single
    fun provideHttpClient(engine: HttpClientEngine, mapper: ObjectMapper,
                          reflectionType: Types, logger: AppLogger, serverPort: Int, attributes: Attributes
    ): HttpClient {
        return HttpClient(engine) {
            expectSuccess = true
            install(pluginClientResponseModalTransformation(mapper, reflectionType))
            pluginClientLogging(this)
            pluginClientAuth(this, logger)
            pluginClientContentNegotiation(this, mapper)
            pluginClientHttpTimeout(this)
            pluginClienDefaultRequest(this, attributes, serverPort)
            pluginClientHttpResponseValidator(this, logger)
        }
    }
}