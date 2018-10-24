package ru.kabylin.andrey.currencyexchange.reactivex.compositors

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.kabylin.andrey.currencyexchange.client.Client
import ru.kabylin.andrey.currencyexchange.client.ClientResponse
import ru.kabylin.andrey.currencyexchange.client.RequestState

class RequestStateRxCompositor(
    private val client: Client,
    private val requestCode: Int = Client.REQUEST_DEFAULT
) : RxCompositor {

    private fun response(requestState: RequestState) =
        ClientResponse(requestState, requestCode, true)

    override fun <T> compose(single: Single<T>): Single<T> {
        return single
            .doOnSubscribe { client.requestState.onNext(response(RequestState.STARTED)) }
            .doAfterTerminate { client.requestState.onNext(response(RequestState.FINISHED)) }
            .doOnDispose { client.requestState.onNext(response(RequestState.FINISHED)) }
    }

    override fun <T> compose(flowable: Flowable<T>): Flowable<T> {
        return flowable
            .doOnSubscribe { client.requestState.onNext(response(RequestState.STARTED)) }
            .doAfterNext { client.requestState.onNext(response(RequestState.NEXT)) }
            .doAfterTerminate { client.requestState.onNext(response(RequestState.FINISHED)) }
            .doOnCancel { client.requestState.onNext(response(RequestState.FINISHED)) }
    }

    override fun <T> compose(observable: Observable<T>): Observable<T> {
        return observable
            .doOnSubscribe { client.requestState.onNext(response(RequestState.STARTED)) }
            .doAfterNext { client.requestState.onNext(response(RequestState.NEXT)) }
            .doAfterTerminate { client.requestState.onNext(response(RequestState.FINISHED)) }
            .doOnDispose { client.requestState.onNext(response(RequestState.FINISHED)) }
    }

    override fun compose(completable: Completable): Completable {
        return completable
            .doOnSubscribe { client.requestState.onNext(response(RequestState.STARTED)) }
            .doAfterTerminate { client.requestState.onNext(response(RequestState.FINISHED)) }
            .doOnDispose { client.requestState.onNext(response(RequestState.FINISHED)) }
    }
}
