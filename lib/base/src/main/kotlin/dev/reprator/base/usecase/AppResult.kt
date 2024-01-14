package dev.reprator.base.usecase

sealed class AppResult<out T> {
    /**
     * Represents successful network responses (2xx).
     */
    data class Success<T>(val body: T) : AppResult<T>()

    sealed class Error<E> : AppResult<E>() {
        /**
         * Represents server errors.
         * @param code HTTP Status code
         * @param errorBody Response body
         * @param errorMessage Custom error message
         */
        data class HttpError<E>(
            val code: Int,
            val errorBody: String?,
            val errorMessage: String?,
        ) : Error<E>()

        /**
         * Represent other exceptions.
         * @param message Detail exception message
         * @param errorMessage Formatted error message
         */
        data class GenericError(
            val message: String?,
            val errorMessage: String?,
        ) : Error<Nothing>()
    }
}