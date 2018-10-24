package ru.kabylin.andrey.currencyexchange.reactivex.compositors

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SchedulerRxCompositor(
    private val backgroundScheduler: Scheduler = Schedulers.io(),
    private val resultScheduler: Scheduler = AndroidSchedulers.mainThread()
) : RxCompositor {
    override fun <T> compose(single: Single<T>): Single<T> {
        return single
            .subscribeOn(backgroundScheduler)
            .observeOn(resultScheduler)
    }

    override fun <T> compose(flowable: Flowable<T>): Flowable<T> {
        return flowable
            .subscribeOn(backgroundScheduler)
            .observeOn(resultScheduler)
    }

    override fun <T> compose(observable: Observable<T>): Observable<T> {
        return observable
            .subscribeOn(backgroundScheduler)
            .observeOn(resultScheduler)
    }

    override fun compose(completable: Completable): Completable {
        return completable
            .subscribeOn(backgroundScheduler)
            .observeOn(resultScheduler)
    }
}
