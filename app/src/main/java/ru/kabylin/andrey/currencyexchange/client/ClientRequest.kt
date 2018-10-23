package ru.kabylin.andrey.currencyexchange.client

import io.reactivex.disposables.Disposable

enum class RequestState {
    STARTED,
    FINISHED
}

class ClientRequest(
    val requestCode: Int = Client.REQUEST_DEFAULT,
    val executor: ClientRequest.() -> Unit,
    internal var disposable: Disposable?,
    val onDispose: (() -> Unit)? = null
) {
    constructor(requestCode: Int = Client.REQUEST_DEFAULT, executor: ClientRequest.() -> Unit)
        : this(requestCode, executor, null)

    private var isDone = false

    fun markAsDone() {
        isDone = true
    }

    fun isDone() = isDone

    fun dispose() {
        disposable?.dispose()
    }

    fun isDisposed(): Boolean =
        disposable?.isDisposed ?: true
}
