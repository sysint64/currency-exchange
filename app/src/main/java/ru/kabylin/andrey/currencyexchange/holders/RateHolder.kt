package ru.kabylin.andrey.currencyexchange.holders

import android.content.Context
import android.view.View
import kotlinx.android.synthetic.main.item_currency.view.*
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import ru.kabylin.andrey.currencyexchange.views.recyclerview.RecyclerItemHolder

class RateHolder(context: Context, view: View) : RecyclerItemHolder<ExchangeService.RateResponse>(context, view) {
    override fun bind(data: ExchangeService.RateResponse) =
        with(view) {
            titleTextView.text = data.title
            descriptionTextView.text = data.description
            countryImageView.setImageResource(data.flag)
            rateEditText.setText(data.value)
        }
}
