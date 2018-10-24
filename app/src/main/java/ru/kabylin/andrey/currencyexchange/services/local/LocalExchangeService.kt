package ru.kabylin.andrey.currencyexchange.services.local

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.kabylin.andrey.currencyexchange.R
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import java.util.*
import java.util.concurrent.TimeUnit

class LocalExchangeService : ExchangeService {
    companion object {
        const val PERIOD = 2L  // Seconds
    }

    private val sampleRates =
        listOf(
            ExchangeService.RateResponse(
                ref = "EUR",
                title = "EUR",
                description = "Euro Member Countries, Euro",
                flag = R.mipmap.flag_eur,
                value = "1"
            ),
            ExchangeService.RateResponse(
                ref = "AUD",
                title = "AUD",
                description = "Australia, Dollar",
                flag = R.mipmap.flag_aud,
                value = "1.6155"
            ),
            ExchangeService.RateResponse(
                ref = "BGN",
                title = "BGN",
                description = "Bulgaria, Lev",
                flag = R.mipmap.flag_bgn,
                value = "1.9547"
            ),
            ExchangeService.RateResponse(
                ref = "BRL",
                title = "BRL",
                description = "Brazil, Real",
                flag = R.mipmap.flag_brl,
                value = "4.789"
            ),
            ExchangeService.RateResponse(
                ref = "CAD",
                title = "CAD",
                description = "Canada, Dollar",
                flag = R.mipmap.flag_cad,
                value = "1.5329"
            ),
            ExchangeService.RateResponse(
                ref = "CHF",
                title = "CHF",
                description = "Switzerland, Franc",
                flag = R.mipmap.flag_chf,
                value = "1.1268"
            ),
            ExchangeService.RateResponse(
                ref = "CNY",
                title = "CNY",
                description = "China, Yuan Renminbi",
                flag = R.mipmap.flag_cny,
                value = "7.9405"
            ),
            ExchangeService.RateResponse(
                ref = "CZK",
                title = "CZK",
                description = "Czechia, Koruna",
                flag = R.mipmap.flag_czk,
                value = "25.7"
            )
        )

    private var factor: Double = 1.0

    override fun getBaseRate(): Single<ExchangeService.RateResponse> =
        Single.just(
            ExchangeService.RateResponse(
                ref = "EUR",
                title = "EUR",
                description = "Euro Member Countries, Euro",
                flag = R.mipmap.flag_eur,
                value = "1"
            )
        )

    override fun rates() =
        Flowable.interval(PERIOD, TimeUnit.SECONDS)
            .map { _ ->
                val random = Random()
                sampleRates
                    .map {
                        it.copy(
                            value = String.format("%.4f", random.nextDouble() * 100)
                        )
                    }
            }

    override fun setBase(baseRef: String, factor: String): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
