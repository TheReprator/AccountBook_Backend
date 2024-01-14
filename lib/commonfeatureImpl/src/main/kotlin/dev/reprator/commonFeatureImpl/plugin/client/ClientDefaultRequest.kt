package dev.reprator.commonFeatureImpl.plugin.client

import dev.reprator.base.beans.*
import dev.reprator.commonFeatureImpl.di.APP_RUNNING_PORT_ADDRESS
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import org.koin.core.Koin
import org.koin.core.qualifier.named

fun pluginClientDefaultRequest(koin: Koin, httpClientConfig: HttpClientConfig<*>) {

    httpClientConfig.defaultRequest {

        val clientAttributes: Attributes = koin.get()

        val providerType = clientAttributes[AttributeKey<APIS>(API_HOST_IDENTIFIER)]
        val isExternal = providerType == APIS.EXTERNAL_OTP_VERIFICATION

        headers {
            append(HttpHeaders.ContentType, "application/json")

            if (isExternal) {
                append("API-Key", koin.get<String>(named(VERIFICATION_SMS_PHONE_APIKEY)))
                append("User-ID", koin.get<String>(named(VERIFICATION_SMS_PHONE_USERID)))
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
                koin.get<Int>(named(APP_RUNNING_PORT_ADDRESS))
            protocol = if (isExternal) URLProtocol.HTTPS else URLProtocol.HTTP
        }
    }
}
