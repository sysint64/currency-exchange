package ru.kabylin.andrey.currencyexchange.client

interface ClientErrorsListener {
    fun onLogicError(error: ClientResponse<LogicError>) {}
    fun onValidationErrors(error: ClientResponse<ValidationErrors>) {}
    fun onCredentialsError(error: ClientResponse<CredentialsError>) {}
    fun onAccessError(error: ClientResponse<AccessError>) {}
    fun onCriticalError(error: ClientResponse<Throwable>) {}
    fun onError(error: ClientResponse<Throwable>) {}
}

