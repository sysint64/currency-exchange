package ru.kabylin.andrey.currencyexchange.services.models

import ru.kabylin.andrey.currencyexchange.client.*

inline fun <T> safeTransform(crossinline transform: () -> T): T {
    try {
        return transform()
    } catch (e: Throwable) {
        throw when (e) {
            is AccessError,
            is CredentialsError,
            is SessionError,
            is ValidationErrors,
            is LogicError -> e
            else -> AccessError(AccessErrorReason.BAD_RESPONSE, e)
        }
    }
}

