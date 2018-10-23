package ru.kabylin.andrey.currencyexchange.reactivex.compositors

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import ru.kabylin.andrey.currencyexchange.client.Client

class ErrorsRxCompositor(
    val client: Client,
    private val requestCode: Int = Client.REQUEST_DEFAULT
) : RxCompositor {

    override fun <T> compose(single: Single<T>): Single<T> {
        return single.doOnError(this::onError)
    }

    override fun <T> compose(flowable: Flowable<T>): Flowable<T> {
        return flowable.doOnError(this::onError)
    }

    override fun <T> compose(observable: Observable<T>): Observable<T> {
        return observable.doOnError(this::onError)
    }

    override fun compose(completable: Completable): Completable {
        return completable.doOnError(this::onError)
    }

    private fun onError(throwable: Throwable) {
        client.onError(throwable, requestCode)
    }
}
