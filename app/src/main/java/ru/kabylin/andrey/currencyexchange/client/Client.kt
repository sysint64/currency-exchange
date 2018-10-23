package ru.kabylin.andrey.currencyexchange.client

import io.reactivex.subjects.PublishSubject
import ru.kabylin.andrey.currencyexchange.reactivex.compositors.RxCompositor
import java.util.concurrent.LinkedBlockingQueue

open class Client {
    companion object {
        const val REQUEST_DEFAULT = 2000

        fun createErrorResponse(reason: DescReason): ClientResponse<LogicError> =
            ClientResponse(LogicError(reason), REQUEST_DEFAULT, false)
    }

    val errors = PublishSubject.create<ClientResponse<Throwable>>()
    val requestState = PublishSubject.create<ClientResponse<RequestState>>()

    val queue = LinkedBlockingQueue<ClientRequest>()

    var compositor: RxCompositor? = null
        set(value) {
            assert(field == null)
            field = value
        }

    fun onError(throwable: Throwable, requestCode: Int = Client.REQUEST_DEFAULT) {
        throwable.printStackTrace()

        fun <T> response(t: T): ClientResponse<T> {
            return ClientResponse(t, requestCode, false)
        }

        errors.onNext(response(throwable))
    }
}
