package dev.reprator.wrapper

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

interface HttpStatusCodeModal {
    val statusCode: Int
}

@Serializable
data class ResultResponse(override val statusCode: Int, @Contextual val data: Any) : HttpStatusCodeModal

@Serializable
data class FailResponse(override val statusCode: Int, @Contextual val error: String) : HttpStatusCodeModal