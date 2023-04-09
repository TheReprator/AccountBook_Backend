package dev.reprator.language.domain

import dev.reprator.core.exception.StatusCodeException

class LanguageEmptyException(message: String = "Empty language list", cause: Throwable? = null) : StatusCodeException.Empty(message, cause)
class LanguageNotFoundException(message: String = "Language didn't exist", cause: Throwable? = null) : StatusCodeException.NotFound(message, cause)
class IllegalLanguageException(message: String? = "Illegal Language info", cause: Throwable? = null) : StatusCodeException.BadRequest(message, cause)