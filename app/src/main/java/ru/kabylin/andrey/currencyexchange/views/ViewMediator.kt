package ru.kabylin.andrey.currencyexchange.views

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.CallSuper
import io.reactivex.disposables.CompositeDisposable

open class ViewMediator(val aware: ViewMediatorAware, private val lifecycle: Lifecycle) : LifecycleObserver {
    val lifecycleDisposer = CompositeDisposable()

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun subscribe() {
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun unsubscribe() {
        aware.unsubscribe()
        lifecycleDisposer.clear()
    }

    @CallSuper
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    open fun refresh() {
        aware.viewStateRefresh()
    }
}
