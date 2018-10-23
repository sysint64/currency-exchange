package ru.kabylin.andrey.currencyexchange.client

interface RequestStateListener {
    fun onRequestStateUpdated(requestState: ClientResponse<RequestState>)
}
