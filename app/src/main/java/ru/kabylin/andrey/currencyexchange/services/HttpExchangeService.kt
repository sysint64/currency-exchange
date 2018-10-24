package ru.kabylin.andrey.currencyexchange.services

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import retrofit2.http.GET
import retrofit2.http.Query
import ru.kabylin.andrey.currencyexchange.R
import ru.kabylin.andrey.currencyexchange.client.ValidationErrors
import ru.kabylin.andrey.currencyexchange.client.http.HttpClient
import ru.kabylin.andrey.currencyexchange.containers.EitherStringRes
import ru.kabylin.andrey.currencyexchange.services.models.ApiRatesResponse
import ru.kabylin.andrey.currencyexchange.services.models.convertApiRatesResponseToRateResponseList
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class HttpExchangeService(client: HttpClient) : ExchangeService {
    companion object {
        const val PERIOD = 1L  // Seconds
    }

    interface ApiGateway {
        @GET("latest")
        fun getRates(@Query("base") base: String): Single<ApiRatesResponse>
    }

    private val apiGateway by lazy {
        client.createRetrofitGateway(
            ApiGateway::class.java,
            HttpClient.Dest.MAIN_API
        )
    }

    private var factor: Double = 1.0
    private var baseRef: String = "EUR"
    private val subject = PublishSubject.create<List<ExchangeService.RateResponse>>()
    private var timerStarted = false
    private var skipCount = 0
    private var currentRates: List<ExchangeService.RateResponse> = listOf()

    override fun getBaseRate(): Single<ExchangeService.RateResponse> =
        Single.just(
            ExchangeService.RateResponse(
                ref = "EUR",
                title = "EUR",
                description = EitherStringRes.res(R.string.description_currency_eur),
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
            factor = 1.0
        }

    override fun refreshRates(): Completable =
        emitNewRates()

    private fun emitNewRates() =
        apiGateway.getRates(baseRef)
            .map {
                currentRates = convertApiRatesResponseToRateResponseList(it)
                    .filter { it.ref != baseRef }

                subject.onNext(recalculateValuesForCurrentRates())
            }
            .ignoreElement()

    override fun updateFactor(factor: String): Completable =
        Completable.fromAction {
            val newFactor = factor.toDoubleOrNull()
                ?: throw ValidationErrors("factor", R.string.bad_factor_validation_error)

            this.factor = newFactor
            skipCount = 1  // Пропустить 1 обновление
            subject.onNext(recalculateValuesForCurrentRates())
        }

    private fun recalculateValuesForCurrentRates(): List<ExchangeService.RateResponse> {
        return currentRates
            .map {
                val newValue = it.value.toBigDecimal() * BigDecimal.valueOf(factor)
                it.copy(value = String.format("%.4f", newValue))
            }
    }
}
