package dev.reprator.modals.country

import dev.reprator.base_ktor.exception.StatusCodeException


class CountryEmptyException(message: String = "Empty Country List", cause: Throwable? = null) : StatusCodeException.Empty(message, cause)
class CountryNotFoundException(message: String = "Country didn't exist", cause: Throwable? = null) : StatusCodeException.NotFound(message, cause)
class IllegalCountryException(message: String = "Illegal Country info", cause: Throwable? = null) : StatusCodeException.BadRequest(message, cause)