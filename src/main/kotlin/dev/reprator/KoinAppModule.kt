package dev.reprator

import dev.reprator.core.util.api.HttpExceptions
import dev.reprator.core.util.constants.*
import dev.reprator.core.util.dbConfiguration.DatabaseFactory
import dev.reprator.core.util.logger.AppLogger
import dev.reprator.dao.DefaultDatabaseFactory
import dev.reprator.impl.LoggerImpl
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val MILLISECONDS = 1000L


val koinAppModule = module {
    single<DatabaseFactory> { params -> DefaultDatabaseFactory(appConfig = params.get()) }
    single<AppLogger> { LoggerImpl() }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }.withOptions {
        qualifier = named(APP_COROUTINE_SCOPE)
    }
}

val koinAppNetworkModule = module {
    single<Attributes> { Attributes(true) }

    single<HttpClient> {
        val clientAttributes: Attributes = get()

        HttpClient(CIO) {

            expectSuccess = true

            engine {
                maxConnectionsCount = 1000
                endpoint {
                    maxConnectionsPerRoute = 100
                    pipelineMaxSize = 20
                    keepAliveTime = 5000
                    connectTimeout = 5000
                    connectAttempts = 5
                }
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


            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
//                filter { request ->
//                    request.url.host.contains("ktor.io")
//                }
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }


            install(ContentNegotiation) {
                jackson()
            }


            install(HttpTimeout) {
                connectTimeoutMillis = 10 * MILLISECONDS
                socketTimeoutMillis = 10 * MILLISECONDS
                requestTimeoutMillis = 10 * MILLISECONDS
            }


            defaultRequest {

                val providerType = clientAttributes[AttributeKey<APIS>("API")]

                headers {
                    append(HttpHeaders.ContentType, "application/json")

                    val apiType = providerType == APIS.EXTERNAL_OTP_VERIFICATION
                    if(apiType) {
                        append("API-Key", VERIFICATION_SMS_PHONE_API)
                        append("User-ID", VERIFICATION_SMS_PHONE_USERID)
                    }
                }

                url {
                    val apiHost = when(providerType) {
                        APIS.EXTERNAL_OTP_VERIFICATION ->
                            API_BASE_URL.EXTERNAL_OTP_VERIFICATION.value
                        else -> API_BASE_URL.INTERNAL_APP.value
                    }
                    host = apiHost
                    //protocol = URLProtocol.HTTPS
                }
            }


            HttpResponseValidator {
                validateResponse { response ->

                    if (!response.status.isSuccess()) {
                        val failureReason = when (response.status) {
                            HttpStatusCode.Unauthorized -> "Unauthorized request"
                            HttpStatusCode.Forbidden -> "${response.status.value} Missing API key."
                            HttpStatusCode.NotFound -> "Invalid Request"
                            HttpStatusCode.UpgradeRequired -> "Upgrade to VIP"
                            HttpStatusCode.RequestTimeout -> "Network Timeout"
                            in HttpStatusCode.InternalServerError..HttpStatusCode.GatewayTimeout ->
                                "${response.status.value} Server Error"

                            else -> "Network error!"
                        }

                        throw HttpExceptions(
                            response = response,
                            failureReason = failureReason,
                            cachedResponseText = response.bodyAsText(),
                        )
                    }
                }
            }
        }
    }
}