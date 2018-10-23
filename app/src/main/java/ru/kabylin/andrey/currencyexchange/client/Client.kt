package ru.kabylin.andrey.currencyexchange.client

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import ru.kabylin.andrey.currencyexchange.reactivex.compositors.ErrorsRxCompositor
import ru.kabylin.andrey.currencyexchange.reactivex.compositors.MergeRxCompositor
import ru.kabylin.andrey.currencyexchange.reactivex.compositors.RequestStateRxCompositor
import ru.kabylin.andrey.currencyexchange.reactivex.compositors.RxCompositor
import java.util.concurrent.LinkedBlockingQueue

@Suppress("MemberVisibilityCanBePrivate")
open class Client {
    companion object {
        const val REQUEST_DEFAULT = 2000

        fun createErrorResponse(reason: DescReason): ClientResponse<LogicError> =
            ClientResponse(LogicError(reason), REQUEST_DEFAULT, false)
    }

    val errors = PublishSubject.create<ClientResponse<Throwable>>()
    val requestState = PublishSubject.create<ClientResponse<RequestState>>()

    val queue = LinkedBlockingQueue<ClientRequest>()
    private var lastRequest: ClientRequest? = null

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

    fun retryLastRequest() {
        assert(lastRequest != null) { "lastRequest can't be null" }
        executeRequest(lastRequest!!)
    }

    private fun executeRequest(request: ClientRequest) {
        try {
            val executor = request.executor
            request.executor()
        } catch (throwable: Exception) {
            onError(throwable, request.requestCode)
        }
    }

    fun <T> execute(query: Single<T>, onSuccess: (ClientResponse<T>) -> Unit = {}): ClientRequest =
        execute(query, Client.REQUEST_DEFAULT, onSuccess)

    fun <T> execute(query: Single<T>, requestCode: Int, onSuccess: (ClientResponse<T>) -> Unit = {}): ClientRequest {
        val clientRequest = RequestBuilder()
            .withQueue(queue)
            .withRequestCode(requestCode)
            .withCompositor(createRequestCompositor(requestCode))
            .buildForSingle(query, onSuccess)

        execute(clientRequest)
        return clientRequest
    }

    fun <T> execute(query: Flowable<T>, requestCode: Int = Client.REQUEST_DEFAULT, onNext: (ClientResponse<T>) -> Unit = {}): ClientRequest =
        execute(query, requestCode, onNext, {})

    fun <T> execute(query: Flowable<T>, onNext: (ClientResponse<T>) -> Unit, onComplete: () -> Unit): ClientRequest =
        execute(query, Client.REQUEST_DEFAULT, onNext, onComplete)

    fun <T> execute(query: Flowable<T>, requestCode: Int, onNext: (ClientResponse<T>) -> Unit, onComplete: () -> Unit): ClientRequest {
        val clientRequest = RequestBuilder()
            .withRequestCode(requestCode)
            .withCompositor(createRequestCompositor(requestCode))
            .buildForFlowable(query, onNext, onComplete)

        execute(clientRequest)
        return clientRequest
    }

    fun <T> execute(query: Observable<T>, requestCode: Int = Client.REQUEST_DEFAULT, onNext: (ClientResponse<T>) -> Unit = {}): ClientRequest =
        execute(query, requestCode, onNext, {})

    fun <T> execute(query: Observable<T>, onNext: (ClientResponse<T>) -> Unit, onComplete: () -> Unit = {}): ClientRequest =
        execute(query, Client.REQUEST_DEFAULT, onNext, onComplete)

    fun <T> execute(query: Observable<T>, requestCode: Int, onNext: (ClientResponse<T>) -> Unit, onComplete: () -> Unit): ClientRequest {
        val clientRequest = RequestBuilder()
            .withRequestCode(requestCode)
            .withCompositor(createRequestCompositor(requestCode))
            .buildForObservable(query, onNext, onComplete)

        execute(clientRequest)
        return clientRequest
    }

    fun execute(query: Completable, onComplete: () -> Unit = {}): ClientRequest =
        execute(query, Client.REQUEST_DEFAULT, onComplete)

    fun execute(query: Completable, requestCode: Int, onComplete: () -> Unit): ClientRequest {
        val clientRequest = RequestBuilder()
            .withRequestCode(requestCode)
            .withCompositor(createRequestCompositor(requestCode))
            .buildForCompletable(query, onComplete)

        execute(clientRequest)
        return clientRequest
    }

    fun execute(request: ClientRequest) {
        executeRequest(request)
        queue.add(request)
        lastRequest = request
    }

    private fun createRequestCompositor(requestCode: Int = Client.REQUEST_DEFAULT): RxCompositor {
        val requestStateCompositor = RequestStateRxCompositor(this, requestCode)
        val errorsCompositor = ErrorsRxCompositor(this, requestCode)
        val requestCompositor = MergeRxCompositor(requestStateCompositor, errorsCompositor)

        return if (compositor == null) {
            requestCompositor
        } else {
            MergeRxCompositor(compositor!!, requestCompositor)
        }
    }
}
