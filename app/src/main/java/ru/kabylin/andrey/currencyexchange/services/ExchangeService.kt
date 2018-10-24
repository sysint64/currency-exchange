package ru.kabylin.andrey.currencyexchange.services

import android.support.annotation.DrawableRes
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface ExchangeService {
    data class RateResponse(
        val ref: String,
        val title: String,
        val description: String,
        val value: String,
        @DrawableRes val flag: Int
    )

    fun getBaseRate(): Single<RateResponse>

    fun rates(): Flowable<List<RateResponse>>

    fun setBase(baseRef: String, factor: String): Completable
}
