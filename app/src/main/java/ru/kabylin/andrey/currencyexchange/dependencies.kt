package ru.kabylin.andrey.currencyexchange

import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import ru.kabylin.andrey.currencyexchange.client.Client
import ru.kabylin.andrey.currencyexchange.client.http.HttpClient
import ru.kabylin.andrey.currencyexchange.client.http.HttpErrorsRxCompositor
import ru.kabylin.andrey.currencyexchange.reactivex.compositors.MergeRxCompositor
import ru.kabylin.andrey.currencyexchange.reactivex.compositors.SchedulerRxCompositor
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import ru.kabylin.andrey.currencyexchange.services.HttpExchangeService
import ru.kabylin.andrey.currencyexchange.services.LocalExchangeService

fun dependencies() = Kodein.Module {
    bind<HttpClient>() with singleton {
        val client = HttpClient()
        client.compositor = MergeRxCompositor(
            SchedulerRxCompositor(),
            HttpErrorsRxCompositor()
        )
        client
    }

    bind<Client>() with provider { instance<HttpClient>() }

    bind<ExchangeService>() with provider { HttpExchangeService(instance()) }
}
