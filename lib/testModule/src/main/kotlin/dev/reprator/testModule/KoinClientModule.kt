package dev.reprator.testModule

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.reprator.core.usecase.JWTConfiguration
import dev.reprator.core.usecase.JwtTokenService
import dev.reprator.core.util.api.HttpExceptions
import dev.reprator.core.util.constants.*
import dev.reprator.core.util.logger.AppLogger
import impl.AppLoggerImpl
import impl.JwtTokenServiceImpl
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.api.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.config.*
import io.ktor.util.*
import org.koin.core.KoinApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.lang.reflect.Type
import java.lang.reflect.WildcardType

private const val MILLISECONDS = 1000L

const val JWT_PARAMETER_SECRET = "sdfrw35r3534"
const val JWT_PARAMETER_AUDIENCE = "JWTaudience"
const val JWT_PARAMETER_ISSUER = "JWTissuer"
const val JWT_PARAMETER_REALM = "realm"

const val JWT_SERVICE = "jwtRealmTest"

var TOKEN_ACCESS: String = ""
var TOKEN_REFRESH: String = ""

fun KoinApplication.setupCoreNetworkModule() {
    modules(appCoreModule, koinAppTestNetworkModule)
}

private val appCoreModule = module {

    single<AppLogger> { AppLoggerImpl() }

    /*    single<(Int) -> Boolean>(named(JWT_SERVICE)) {
             {
               true
            }
        }*/

    single<JwtTokenService> {
        JwtTokenServiceImpl(
            JWTConfiguration(
                JWT_PARAMETER_SECRET, JWT_PARAMETER_AUDIENCE, JWT_PARAMETER_ISSUER,
                JWT_PARAMETER_REALM
            ), isUserValid = get<(Int) -> Boolean>(named(JWT_SERVICE))
        )
    }
}

private val koinAppTestNetworkModule = module {
    single<Int> { ApplicationConfig("application-test.conf").property("ktor.deployment.port").getString().toInt() }

    single<ObjectMapper> {
        jacksonObjectMapper().apply {
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            configure(SerializationFeature.INDENT_OUTPUT, true)
            setDefaultPrettyPrinter(DefaultPrettyPrinter().apply {
                indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            })
        }
    }
    single<HttpClientEngine> { CIO.create() }

    single<Attributes> { Attributes(true) }

    single<HttpClient> {

        val engine: HttpClientEngine = get()

        HttpClient(engine) {

            expectSuccess = true

            install(DataTransformationPlugin)

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }


            install(Auth) {
                bearer {
                    val logger by inject<AppLogger>()

                    loadTokens {
                        logger.e { "vikram:: install :: loadToken, $TOKEN_ACCESS, $TOKEN_REFRESH" }
                        BearerTokens(TOKEN_ACCESS, TOKEN_REFRESH)
                    }

                    refreshTokens {
                        logger.e { "vikram:: install :: refreshTokens, $TOKEN_ACCESS, $TOKEN_REFRESH" }
                        BearerTokens(TOKEN_ACCESS, TOKEN_REFRESH)
                    }

                    sendWithoutRequest { request ->
                        logger.e { "vikram:: install :: sendWithoutRequest:: ${request.url.host}" }
                        request.url.host == "www.googleapis.com"
                    }
                }
            }

            install(ContentNegotiation) {
                jackson {
                    get<ObjectMapper>()
                }
            }


            install(HttpTimeout) {
                connectTimeoutMillis = 60 * MILLISECONDS
                socketTimeoutMillis = 60 * MILLISECONDS
                requestTimeoutMillis = 60 * MILLISECONDS
            }


            defaultRequest {
                val clientAttributes: Attributes = get()

                val providerType = clientAttributes[AttributeKey<APIS>(API_HOST_IDENTIFIER)]

                val isExternal = providerType == APIS.EXTERNAL_OTP_VERIFICATION

                headers {
                    append(HttpHeaders.ContentType, "application/json")

                    if (isExternal) {
                        append("API-Key", VERIFICATION_SMS_PHONE_APIKEY)
                        append("User-ID", VERIFICATION_SMS_PHONE_USERID)
                    }
                }

                url {
                    val apiHost = when (providerType) {
                        APIS.EXTERNAL_OTP_VERIFICATION ->
                            API_BASE_URL.EXTERNAL_OTP_VERIFICATION.value

                        else -> API_BASE_URL.INTERNAL_APP.value
                    }
                    host = apiHost
                    port = if (isExternal)
                        0
                    else
                        get<Int>()
                    protocol = if (isExternal) URLProtocol.HTTPS else URLProtocol.HTTP
                }
            }


            HttpResponseValidator {
                val logger = get<AppLogger>()

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

                        val responseBodyTest = response.bodyAsText()
                        logger.e { "vikramTest:: koinTest:: validateResponse:: $response" }
                        logger.e { "vikramTest:: koinTest:: validateResponse1:: $failureReason" }
                        logger.e { "vikramTest:: koinTest:: validateResponse1:: $responseBodyTest" }

                        throw HttpExceptions(
                            response = response,
                            failureReason = failureReason,
                            cachedResponseText = response.bodyAsText(),
                        )
                    }
                }

                handleResponseExceptionWithRequest { exception, _ ->
                    val clientException =
                        exception as? ClientRequestException ?: return@handleResponseExceptionWithRequest
                    val exceptionResponse = clientException.response
                    val exceptionResponseText = exceptionResponse.bodyAsText()

                    logger.e { "vikramTest:: koinTest:: handleResponseExceptionWithRequest:: $exceptionResponse" }
                    logger.e { "vikramTest:: koinTest:: handleResponseExceptionWithRequest1:: $exceptionResponseText" }

                    throw HttpExceptions(
                        response = exceptionResponse,
                        failureReason = exceptionResponseText,
                        cachedResponseText = exceptionResponseText,
                    )
                }
            }
        }
    }
}

val DataTransformationPlugin =

    createClientPlugin("DataTransformationPlugin") {

        transformResponseBody { response, _, requestedType ->

            val mapper = jacksonObjectMapper()

            fun <T> responseParent(): suspend (Class<T>) -> T? = {
                try {
                    val envelopeType: Type = Types.getInstance().newParameterizedType(it, requestedType.reifiedType)
                    val javaType: JavaType = mapper.constructType(envelopeType)
                    mapper.readValue(response.bodyAsText(), javaType)
                } catch (e: IllegalArgumentException) {
                    println("exception IllegalArgumentException localizedMessage")
                    mapper.readValue(response.bodyAsText(), it)
                } catch (e: Exception) {
                    println("exception localizedMessage")
                    e.stackTrace
                    null
                }
            }

            if (response.status.isSuccess()) {
                responseParent<EnvelopeResponse<*>>()(EnvelopeResponse::class.java)?.data
            } else {
                response.bodyAsText()
            }
        }
    }

fun MockRequestHandleScope.errorResponse(): HttpResponseData {
    return respond(
        content = "",
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType, "application/json")
    )
}


@JsonInclude(JsonInclude.Include.NON_NULL)
class EnvelopeResponse<T>(
    val statusCode: Int,
    val data: T
)