package ru.kabylin.andrey.currencyexchange.services

import android.support.annotation.DrawableRes
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.kabylin.andrey.currencyexchange.containers.EitherStringRes

interface ExchangeService {
    data class RateResponse(
        val ref: String,
        val title: String,
        val description: EitherStringRes,
        val value: String,
        @DrawableRes val flag: Int
    )

    fun getBaseRate(): Single<RateResponse>

    fun rates(): Observable<List<RateResponse>>

    fun setBase(baseRef: String): Completable

    fun refreshRates(): Completable

    fun updateFactor(factor: String): Completable
}
