package ru.kabylin.andrey.currencyexchange.holders

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import kotlinx.android.synthetic.main.item_currency.view.*
import ru.kabylin.andrey.currencyexchange.MainActivity
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import ru.kabylin.andrey.currencyexchange.views.recyclerview.RecyclerItemHolder

class RateHolder(context: Context, view: View) : RecyclerItemHolder<MainActivity.RateRecyclerItemBox>(context, view) {
    private var textWatcher: TextWatcher? = null

    override fun bind(data: MainActivity.RateRecyclerItemBox) =
        with(view) {
            titleTextView.text = data.response.title
            descriptionTextView.text = data.response.description
            countryImageView.setImageResource(data.response.flag)
            rateEditText.setText(data.response.value)

            textWatcher = object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    data.onUpdateRate?.invoke(s.toString())
                }
            }

            if (data.onUpdateRate != null) {
                rateEditText.addTextChangedListener(textWatcher)
            } else if (textWatcher != null) {
                rateEditText.removeTextChangedListener(textWatcher)
            }
        }

    override fun setOnItemClick(data: MainActivity.RateRecyclerItemBox, onItemClick: (data: MainActivity.RateRecyclerItemBox) -> Unit) {
        view.rateEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                onItemClick(data)
            }
        }
    }
}
