package dev.reprator.testModule.plugin

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import dev.reprator.testModule.AppReflectionTypes
import io.ktor.client.plugins.api.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.koin.core.Koin
import java.lang.reflect.Type

fun pluginClientResponseTransformation(koin: Koin): ClientPlugin<Unit> {

    return createClientPlugin("PluginClientResponseTransformation") {

        transformResponseBody { response, _, requestedType ->

            val mapper = koin.get<ObjectMapper>()
            val reflectionType = koin.get<AppReflectionTypes>()

            fun <T> responseParent(): suspend (Class<T>) -> T? = {
                try {
                    val envelopeType: Type = reflectionType.newParameterizedType(it, requestedType.reifiedType)
                    val javaType: JavaType = mapper.constructType(envelopeType)
                    mapper.readValue(response.bodyAsText(), javaType)
                } catch (e: IllegalArgumentException) {
                    println("PluginClientResponseTransformation localizedMessage")
                    mapper.readValue(response.bodyAsText(), it)
                } catch (e: Exception) {
                    println("PluginClientResponseTransformation localizedMessage")
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
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class EnvelopeResponse<T>(
    val statusCode: Int,
    val data: T
)