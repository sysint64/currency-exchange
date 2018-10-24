package ru.kabylin.andrey.currencyexchange

import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton
import ru.kabylin.andrey.currencyexchange.client.Client
import ru.kabylin.andrey.currencyexchange.reactivex.compositors.SchedulerRxCompositor
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import ru.kabylin.andrey.currencyexchange.services.local.LocalExchangeService

fun dependencies() = Kodein.Module {
    bind<Client>() with singleton {
        val client = Client()
        client.compositor = SchedulerRxCompositor()
        client
    }

    bind<ExchangeService>() with provider { LocalExchangeService() }
}
