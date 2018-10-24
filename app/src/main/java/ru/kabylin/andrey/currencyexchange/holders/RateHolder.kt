package ru.kabylin.andrey.currencyexchange.holders

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_currency.view.*
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import ru.kabylin.andrey.currencyexchange.views.recyclerview.RecyclerItemHolder

class RateHolder(context: Context, view: View) : RecyclerItemHolder<ExchangeService.RateResponse>(context, view) {
    override fun bind(data: ExchangeService.RateResponse) =
        with(view) {
            titleTextView.text = data.title
            descriptionTextView.text = data.description.toString(context)

            // Avoid OOM
            Glide
                .with(context)
                .load(data.flag)
                .into(countryImageView)

            rateEditText.setText(data.value)
            rateEditText.tag = data.title
        }

    override fun setOnItemClick(data: ExchangeService.RateResponse, onItemClick: (data: ExchangeService.RateResponse) -> Unit) {
        view.rateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                onItemClick(data)
            }
        }
    }
}
