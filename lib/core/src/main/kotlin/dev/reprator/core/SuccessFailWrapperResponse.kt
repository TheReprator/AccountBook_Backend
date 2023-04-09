package dev.reprator.core


interface HttpStatusCodeModal {
    val statusCode: Int
}


data class ResultResponse<T>(override val statusCode: Int, val data: T) : HttpStatusCodeModal

data class FailResponse(override val statusCode: Int, val error: String) : HttpStatusCodeModal