package ru.kabylin.andrey.currencyexchange.client

data class ClientResponse<out T>(
    val payload: T,
    val requestCode: Int = Client.REQUEST_DEFAULT,
    val success: Boolean
)

