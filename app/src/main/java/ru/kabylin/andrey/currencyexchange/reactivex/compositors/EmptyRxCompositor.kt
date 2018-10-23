package ru.kabylin.andrey.currencyexchange.reactivex.compositors

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

class EmptyRxCompositor : RxCompositor {
    override fun <T> compose(single: Single<T>): Single<T> {
        return single
    }

    override fun <T> compose(flowable: Flowable<T>): Flowable<T> {
        return flowable
    }

    override fun <T> compose(observable: Observable<T>): Observable<T> {
        return observable
    }

    override fun compose(completable: Completable): Completable {
        return completable
    }
}

