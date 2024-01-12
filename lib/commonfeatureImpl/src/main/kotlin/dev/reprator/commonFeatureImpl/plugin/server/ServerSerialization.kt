package dev.reprator.commonFeatureImpl.plugin.server

import io.ktor.serialization.jackson.*
import com.fasterxml.jackson.databind.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import org.koin.ktor.ext.inject

fun Application.configureContentNegotiation() {

    val mapper by inject<ObjectMapper>()

    install(ContentNegotiation) {
        jackson {
            mapper
        }
    }
}
