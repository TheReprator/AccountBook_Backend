package dev.reprator.base.usecase


interface HttpStatusCodeModal {
    val statusCode: Int
}


data class ResultDTOResponse<T>(override val statusCode: Int, val data: T) : HttpStatusCodeModal

data class FailDTOResponse(override val statusCode: Int, val error: String) : HttpStatusCodeModal