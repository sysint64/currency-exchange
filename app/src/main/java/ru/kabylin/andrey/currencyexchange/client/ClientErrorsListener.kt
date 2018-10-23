package ru.kabylin.andrey.currencyexchange.client

interface ClientErrorsListener {
    fun onLogicError(error: ClientResponse<LogicError>) {}
    fun onValidationErrors(error: ClientResponse<ValidationErrors>) {}
    fun onCredentialsError(error: ClientResponse<CredentialsError>) {}
    fun onAccessError(error: ClientResponse<AccessError>) {}
    fun onCriticalError(error: ClientResponse<Throwable>) {}
    fun onError(error: ClientResponse<Throwable>) {}

    companion object {
        fun routeErrorsToListener(response: ClientResponse<Throwable>, listener: ClientErrorsListener) {
            fun <T> createResponse(t: T): ClientResponse<T> {
                return ClientResponse(t, response.requestCode, false)
            }

            listener.onError(response)

            when (response.payload) {
                is LogicError -> listener.onLogicError(createResponse(response.payload))
                is ValidationErrors -> listener.onValidationErrors(createResponse(response.payload))
                is CredentialsError -> listener.onCredentialsError(createResponse(response.payload))
                is AccessError -> listener.onAccessError(createResponse(response.payload))
                else -> listener.onCriticalError(response)
            }
        }
    }
}
