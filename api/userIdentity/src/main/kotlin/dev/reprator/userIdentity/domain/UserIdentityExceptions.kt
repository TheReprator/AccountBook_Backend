package dev.reprator.userIdentity.domain

import dev.reprator.core.exception.StatusCodeException

class UserIdentityEmptyException(message: String = "Empty UserInfo list", cause: Throwable? = null) : StatusCodeException.Empty(message, cause)
class UserIdentityNotFoundException(message: String = "User didn't exist", cause: Throwable? = null) : StatusCodeException.NotFound(message, cause)
class IllegalUserIdentityException(message: String? = "Illegal User info", cause: Throwable? = null) : StatusCodeException.BadRequest(message, cause)
class UserIdentityAlreadyExistException(message: String? = "User already exist", cause: Throwable? = null) : StatusCodeException.BadRequest(message, cause)