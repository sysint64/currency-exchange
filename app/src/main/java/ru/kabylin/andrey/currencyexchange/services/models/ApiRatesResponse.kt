package ru.kabylin.andrey.currencyexchange.services.models

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import ru.kabylin.andrey.currencyexchange.R
import ru.kabylin.andrey.currencyexchange.containers.EitherStringRes
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import java.math.BigDecimal

data class ApiRatesResponse(
    val base: String,
    val date: String,
    val rates: Map<String, BigDecimal>
)

fun convertApiRatesResponseToRateResponseList(apiResponse: ApiRatesResponse): List<ExchangeService.RateResponse> =
    safeTransform {
        data class DataMap(
            @StringRes val description: Int,
            @DrawableRes val flag: Int
        )

        val currencyDataMapping = mapOf(
            "AUD" to DataMap(R.string.description_currency_aud, R.mipmap.flag_aud),
            "BGN" to DataMap(R.string.description_currency_bgn, R.mipmap.flag_bgn),
            "BRL" to DataMap(R.string.description_currency_brl, R.mipmap.flag_brl),
            "CAD" to DataMap(R.string.description_currency_cad, R.mipmap.flag_cad),
            "CHF" to DataMap(R.string.description_currency_chf, R.mipmap.flag_chf),
            "CNY" to DataMap(R.string.description_currency_cny, R.mipmap.flag_cny),
            "CZK" to DataMap(R.string.description_currency_czk, R.mipmap.flag_czk),
            "DKK" to DataMap(R.string.description_currency_dkk, R.mipmap.flag_dkk),
            "GBP" to DataMap(R.string.description_currency_gbp, R.mipmap.flag_gbp),
            "HKD" to DataMap(R.string.description_currency_hkd, R.mipmap.flag_hkd),
            "HRK" to DataMap(R.string.description_currency_hrk, R.mipmap.flag_hrk),
            "HUF" to DataMap(R.string.description_currency_huf, R.mipmap.flag_huf),
            "IDR" to DataMap(R.string.description_currency_idr, R.mipmap.flag_idr),
            "ILS" to DataMap(R.string.description_currency_ils, R.mipmap.flag_ils),
            "INR" to DataMap(R.string.description_currency_inr, R.mipmap.flag_inr),
            "ISK" to DataMap(R.string.description_currency_isk, R.mipmap.flag_isk),
            "JPY" to DataMap(R.string.description_currency_jpy, R.mipmap.flag_jpy),
            "KRW" to DataMap(R.string.description_currency_krw, R.mipmap.flag_krw),
            "MXN" to DataMap(R.string.description_currency_mxn, R.mipmap.flag_mxn),
            "MYR" to DataMap(R.string.description_currency_myr, R.mipmap.flag_myr),
            "NOK" to DataMap(R.string.description_currency_nok, R.mipmap.flag_nok),
            "NZD" to DataMap(R.string.description_currency_nzd, R.mipmap.flag_nzd),
            "PHP" to DataMap(R.string.description_currency_php, R.mipmap.flag_php),
            "PLN" to DataMap(R.string.description_currency_pln, R.mipmap.flag_pln),
            "RON" to DataMap(R.string.description_currency_ron, R.mipmap.flag_ron),
            "RUB" to DataMap(R.string.description_currency_rub, R.mipmap.flag_rub),
            "SEK" to DataMap(R.string.description_currency_sek, R.mipmap.flag_sek),
            "SGD" to DataMap(R.string.description_currency_sgd, R.mipmap.flag_sgd),
            "THB" to DataMap(R.string.description_currency_thb, R.mipmap.flag_thb),
            "TRY" to DataMap(R.string.description_currency_try, R.mipmap.flag_try),
            "USD" to DataMap(R.string.description_currency_usd, R.mipmap.flag_usd),
            "ZAR" to DataMap(R.string.description_currency_zar, R.mipmap.flag_zar)
        )

        apiResponse.rates.keys
            .map {

                ExchangeService.RateResponse(
                    ref = it,
                    title = it,
                    description = if (currencyDataMapping[it] != null) {
                        EitherStringRes.res(currencyDataMapping[it]!!.description)
                    } else {
                        EitherStringRes.string("-")
                    },
                    value = apiResponse.rates[it].toString(),
                    flag = currencyDataMapping[it]?.flag ?: R.mipmap.flag_unknown
                )
            }
    }
