package dev.reprator.commonFeatureImpl.di

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.reprator.base.action.*
import dev.reprator.base.beans.APP_COROUTINE_SCOPE
import dev.reprator.base.beans.UPLOAD_FOLDER_SPLASH
import dev.reprator.base.beans.VERIFICATION_SMS_PHONE_APIKEY
import dev.reprator.base.beans.VERIFICATION_SMS_PHONE_USERID
import dev.reprator.commonFeatureImpl.imp.AppLoggerImpl
import dev.reprator.commonFeatureImpl.imp.DefaultDatabaseFactory
import dev.reprator.commonFeatureImpl.imp.JwtTokenServiceImpl
import dev.reprator.commonFeatureImpl.imp.TokenStorageImpl
import dev.reprator.commonFeatureImpl.plugin.client.pluginClientDefaultRequest
import dev.reprator.commonFeatureImpl.plugin.client.pluginClientResponseAuth
import dev.reprator.commonFeatureImpl.plugin.client.pluginClientResponseValidator
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import propertyConfig

private const val MILLISECONDS = 1000L
const val KEY_SERVICE_SHUTDOWN = "shutDownUrl"
const val APP_PLUGIN_CUSTOM_LIST = "appCustomPlugin"
const val APP_RUNNING_PORT_ADDRESS = "appRunningAddress"

const val JWT_SERVICE = "appJwtRealm"
const val APP_JWT_TOKEN_ACCESS = "appJwtAccess"
const val APP_JWT_TOKEN_REFRESH = "appJwtRefresh"

fun koinAppCommonModule(config: ApplicationConfig) = module {

    single<AppLogger> { AppLoggerImpl() }

    single<TokenStorage> { TokenStorageImpl() }

    factory(named(UPLOAD_FOLDER_SPLASH)) {
        config.propertyConfig(UPLOAD_FOLDER_SPLASH)
    }

    factory(named(VERIFICATION_SMS_PHONE_APIKEY)) {
        config.propertyConfig(
            VERIFICATION_SMS_PHONE_APIKEY
        )
    }

    factory(named(KEY_SERVICE_SHUTDOWN)) {
        config.propertyConfig("ktor.deployment.$KEY_SERVICE_SHUTDOWN.url")
    }

    factory(named(VERIFICATION_SMS_PHONE_USERID)) {
        config.propertyConfig(
            VERIFICATION_SMS_PHONE_USERID
        )
    }

    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }.withOptions {
        qualifier = named(APP_COROUTINE_SCOPE)
    }

    single<(Int) -> Boolean>(named(JWT_SERVICE)) {
        {true}
    }

    single<Long>(named(APP_JWT_TOKEN_ACCESS)) {
        12 * JwtTokenService.HOUR_1_MILLISECONDS
    }

    single<Long>(named(APP_JWT_TOKEN_REFRESH)) {
        24 * JwtTokenService.HOUR_1_MILLISECONDS
    }

    single<JwtTokenService> {
        val property: (String) -> String = {
            config.propertyConfig("jwt.$it")
        }
        val jwtConfiguration =
            JWTConfiguration(property("secret"), property("audience"), property("issuer"), property("realm"))

        JwtTokenServiceImpl(jwtConfiguration,get(named(APP_JWT_TOKEN_ACCESS)), get(named(APP_JWT_TOKEN_REFRESH)), get(named(JWT_SERVICE)))
    }
}


val koinAppCommonDBModule = module {
    single<AppDatabaseFactory> {
        DefaultDatabaseFactory(get(), get())
    }
}


val koinAppNetworkClientModule = module {

    factory<List<ClientPlugin<Unit>>>(named(APP_PLUGIN_CUSTOM_LIST)) {
        emptyList()
    }

    single<ObjectMapper> {
        jacksonObjectMapper().apply {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            })
        }
    }

    single<HttpClientEngine> {
        CIO.create {
            maxConnectionsCount = 1000

            endpoint {
                maxConnectionsPerRoute = 100
                pipelineMaxSize = 20
                keepAliveTime = 5000
                connectTimeout = 5000
                connectAttempts = 5
            }
        }
    }

    single<Attributes> { Attributes(true) }

    single<HttpClient> {

        val engine: HttpClientEngine = get()

        HttpClient(engine) {

            expectSuccess = true

            get<List<ClientPlugin<Unit>>>(named(APP_PLUGIN_CUSTOM_LIST)).forEach {
                install(it)
            }

            install(Logging) {

                logger = Logger.DEFAULT
                level = LogLevel.ALL
//                filter { request ->
//                    request.url.host.contains("ktor.io")
//                }
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }

//            install(HttpRequestRetry) {
//                maxRetries = 5
//                retryIf { _, response ->
//                    !response.status.isSuccess()
//                }
//                retryOnExceptionIf { _, cause ->
//                    cause is IOException
//                }
//                delayMillis { retry ->
//                    retry * 3000L
//                } // retries in 3, 6, 9, etc. seconds
//            }


            install(ContentNegotiation) {
                jackson {
                    get<ObjectMapper>()
                }
            }


            install(HttpTimeout) {
                connectTimeoutMillis = 10 * MILLISECONDS
                socketTimeoutMillis = 10 * MILLISECONDS
                requestTimeoutMillis = 10 * MILLISECONDS
            }

            pluginClientDefaultRequest(getKoin(), this)
            pluginClientResponseAuth(getKoin(), this)
            pluginClientResponseValidator(getKoin(), this)
        }
    }
}