package ru.kabylin.andrey.currencyexchange.client.view

import android.arch.lifecycle.Lifecycle
import ru.kabylin.andrey.currencyexchange.client.*
import ru.kabylin.andrey.currencyexchange.reactivex.disposeBy
import ru.kabylin.andrey.currencyexchange.views.ViewMediator
import ru.kabylin.andrey.currencyexchange.views.ViewMediatorAware

class ClientViewMediator(val client: Client, aware: ViewMediatorAware, lifecycle: Lifecycle)
    : ViewMediator(aware, lifecycle)
{
    override fun subscribe() {
        super.subscribe()

        if (aware is RequestStateListener) {
            client.requestState.subscribe {
                aware.onRequestStateUpdated(it)
            }
            .disposeBy(lifecycleDisposer)
        }

        if (aware is ClientErrorsListener) {
            client.errors.subscribe { response ->
                ClientErrorsListener.routeErrorsToListener(response, aware)
            }
            .disposeBy(lifecycleDisposer)
        }
    }
}
