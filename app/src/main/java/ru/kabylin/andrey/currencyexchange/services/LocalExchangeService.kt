package ru.kabylin.andrey.currencyexchange.services

import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.kabylin.andrey.currencyexchange.R
import ru.kabylin.andrey.currencyexchange.client.ValidationErrors
import ru.kabylin.andrey.currencyexchange.containers.EitherStringRes
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Локальный - тестовый сервис, для возможности проверить бизнес логику
 * без использования http.
 */
class LocalExchangeService : ExchangeService {
    companion object {
        const val PERIOD = 1L  // Seconds
    }

    private val sampleRates =
        listOf(
            ExchangeService.RateResponse(
                ref = "EUR",
                title = "EUR",
                description = EitherStringRes.string("Euro Member Countries, Euro"),
                flag = R.mipmap.flag_eur,
                value = "1"
            ),
            ExchangeService.RateResponse(
                ref = "AUD",
                title = "AUD",
                description = EitherStringRes.string("Australia, Dollar"),
                flag = R.mipmap.flag_aud,
                value = "1.6155"
            ),
            ExchangeService.RateResponse(
                ref = "BGN",
                title = "BGN",
                description = EitherStringRes.string("Bulgaria, Lev"),
                flag = R.mipmap.flag_bgn,
                value = "1.9547"
            ),
            ExchangeService.RateResponse(
                ref = "BRL",
                title = "BRL",
                description = EitherStringRes.string("Brazil, Real"),
                flag = R.mipmap.flag_brl,
                value = "4.789"
            ),
            ExchangeService.RateResponse(
                ref = "CAD",
                title = "CAD",
                description = EitherStringRes.string("Canada, Dollar"),
                flag = R.mipmap.flag_cad,
                value = "1.5329"
            ),
            ExchangeService.RateResponse(
                ref = "CHF",
                title = "CHF",
                description = EitherStringRes.string("Switzerland, Franc"),
                flag = R.mipmap.flag_chf,
                value = "1.1268"
            ),
            ExchangeService.RateResponse(
                ref = "CNY",
                title = "CNY",
                description = EitherStringRes.string("China, Yuan Renminbi"),
                flag = R.mipmap.flag_cny,
                value = "7.9405"
            ),
            ExchangeService.RateResponse(
                ref = "CZK",
                title = "CZK",
                description = EitherStringRes.string("Czechia, Koruna"),
                flag = R.mipmap.flag_czk,
                value = "25.7"
            )
        )

    private var factor: Float = 1.0f
    private var baseRef: String = "EUR"
    private val subject = PublishSubject.create<List<ExchangeService.RateResponse>>()
    private var timerStarted = false
    private var skipCount = 0

    override fun getBaseRate(): Single<ExchangeService.RateResponse> =
        Single.just(
            ExchangeService.RateResponse(
                ref = "EUR",
                title = "EUR",
                description = EitherStringRes.string("Euro Member Countries, Euro"),
                flag = R.mipmap.flag_eur,
                value = "1"
            )
        )

    override fun rates(): Observable<List<ExchangeService.RateResponse>> {
        if (!timerStarted) {
            timerStarted = true

            Flowable.interval(PERIOD, TimeUnit.SECONDS)
                .flatMap {
                    if (skipCount <= 0) {
                        emitNewRates().toFlowable<Unit>()
                    } else {
                        skipCount -= 1
                        Flowable.just(Unit)
                    }
                }
                .subscribe()
        }

        return subject
    }

    override fun setBase(baseRef: String): Completable =
        Completable.fromAction {
            this.baseRef = baseRef
            skipCount = 2  // Пропустить 2 обновления
            factor = 1.0f
        }

    override fun refreshRates(): Completable =
        emitNewRates()

    private fun emitNewRates() = Completable.fromAction {
        val result = sampleRates
            .filter { it.ref != baseRef }
            .map {
                val random = Random()
                it.copy(value = String.format("%.4f", random.nextFloat() * 10 * factor))
            }

        subject.onNext(result)
    }

    override fun updateFactor(factor: String): Completable {
        val newFactor = factor.toFloatOrNull()
            ?: return Completable.error(
                ValidationErrors("factor", R.string.bad_factor_validation_error)
            )

        this.factor = newFactor
        skipCount = 1  // Пропустить 1 обновление
        return emitNewRates()
    }
}
