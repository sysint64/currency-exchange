package ru.kabylin.andrey.currencyexchange.reactivex

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun <T> Single<T>.subscribeOnSuccess(onSuccess: (T) -> Unit): Disposable =
    subscribe(onSuccess, {})

fun Completable.subscribeOnComplete(onComplete: () -> Unit): Disposable =
    subscribe(onComplete, {})

fun <T> Flowable<T>.subscribeOnNext(onNext: (T) -> Unit): Disposable =
    subscribe(onNext, {})

fun <T> Observable<T>.subscribeOnNext(onNext: (T) -> Unit): Disposable =
    subscribe(onNext, {})

fun Disposable.disposeBy(disposables: CompositeDisposable) {
    disposables.add(this)
}
