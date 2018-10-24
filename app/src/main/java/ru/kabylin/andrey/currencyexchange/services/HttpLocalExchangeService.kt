package ru.kabylin.andrey.currencyexchange.services

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import retrofit2.http.GET
import ru.kabylin.andrey.currencyexchange.R
import ru.kabylin.andrey.currencyexchange.client.ValidationErrors
import ru.kabylin.andrey.currencyexchange.client.http.HttpClient
import ru.kabylin.andrey.currencyexchange.containers.EitherStringRes
import ru.kabylin.andrey.currencyexchange.services.models.ApiRatesResponse
import java.util.concurrent.TimeUnit

class HttpExchangeService(client: HttpClient) : ExchangeService {
    companion object {
        const val PERIOD = 1L  // Seconds
    }

    interface ApiGateway {
        @GET("latest")
        fun getRates(): Single<ApiRatesResponse>
    }

    private val apiGateway by lazy {
        client.createRetrofitGateway(
            ApiGateway::class.java,
            HttpClient.Dest.MAIN_API
        )
    }

    private var factor: Float = 1.0f
    private var baseRef: String = "EUR"
    private val subject = PublishSubject.create<List<ExchangeService.RateResponse>>()
    private var timerStarted = false
    private var skipCount = 0
    private val currentRates = ArrayList<ExchangeService.RateResponse>()

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
//        val result = sampleRates
//            .filter { it.ref != baseRef }
//            .map {
//                val random = Random()
//                it.copy(value = String.format("%.4f", random.nextFloat() * 10 * factor))
//            }
//
//        subject.onNext(result)
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
