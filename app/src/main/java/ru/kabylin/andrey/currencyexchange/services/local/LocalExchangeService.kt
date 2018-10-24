package ru.kabylin.andrey.currencyexchange.services.local

import io.reactivex.*
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import ru.kabylin.andrey.currencyexchange.R
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
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
    private var baseRef: String = "EUR"
    private val subject = PublishSubject.create<List<ExchangeService.RateResponse>>()
    private var timerStarted = false
    private var skipCount = 0
    private val lock = Object()

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

    override fun rates(): Observable<List<ExchangeService.RateResponse>> {
        if (!timerStarted) {
            timerStarted = true

            Flowable.interval(PERIOD, TimeUnit.SECONDS)
                .subscribeBy { _ ->
                    if (skipCount <= 0) {
                        val result = sampleRates.filter { it.ref != baseRef }
                        subject.onNext(result)
                    } else {
                        skipCount -= 1
                    }
                }
        }

        return subject
    }

    override fun setBase(baseRef: String, factor: String): Completable =
        Completable.fromAction {
            this.baseRef = baseRef
            skipCount = 2  // Пропустить 2 обновления
        }

    override fun refreshRates(): Completable =
        Completable.fromAction {
            val result = sampleRates.filter { it.ref != baseRef }
            subject.onNext(result)
        }
}
