package ru.kabylin.andrey.currencyexchange

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_currency.view.*
import kotlinx.android.synthetic.main.part_connection_error.view.*
import kotlinx.android.synthetic.main.part_errors.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import org.kodein.di.generic.kcontext
import ru.kabylin.andrey.currencyexchange.client.*
import ru.kabylin.andrey.currencyexchange.client.view.ClientViewMediator
import ru.kabylin.andrey.currencyexchange.ext.hideView
import ru.kabylin.andrey.currencyexchange.ext.showView
import ru.kabylin.andrey.currencyexchange.holders.RateHolder
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import ru.kabylin.andrey.currencyexchange.views.ViewMediatorAware
import ru.kabylin.andrey.currencyexchange.views.recyclerview.SingleItemRecyclerAdapter

/**
 * TODO: Необходимо отображать все ошибки, и написать UI тесты для проверки,
 * что на всех экранах корректно будут отображены ошибки пользователю.
 * Также можно инкапсулировать отображение ошибок по умолчанию.
 */
class MainActivity : AppCompatActivity(), KodeinAware, ViewMediatorAware,
    ClientErrorsListener, RequestStateListener
{
    companion object {
        const val SUBSCRIBE_REQUEST_CODE = 2001
    }

    override val kodeinContext = kcontext(this)
    override val kodein by closestKodein()

    private val client: Client by instance()
    private val exchangeService: ExchangeService by instance()

    override val viewMediator: ClientViewMediator by lazy {
        ClientViewMediator(client, this, lifecycle)
    }

    private val items = ArrayList<ExchangeService.RateResponse>()
    private var base: ExchangeService.RateResponse? = null
    private var lastRate = ""

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            /**
             * Т.к. recyclerView переиспользует editText, удостоверимся, что
             * используется верное поле для редактирования множителя.
             */
            if (getFactorEditText()?.tag == base?.ref)
                onUpdateRateFactor(s.toString())
        }
    }

    private val recyclerAdapter by lazy {
        SingleItemRecyclerAdapter(this, items, R.layout.item_currency,
            ::RateHolder, ::onRateClick)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.adapter = recyclerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        viewMediator.onCreate()

        noInternetErrorIncludeView.retryButton.setOnClickListener(::onRetryButtonClick)
        timeIsOutErrorIncludeView.retryButton.setOnClickListener(::onRetryButtonClick)
    }

    override fun subscribe() {
        items.clear()
        val query = exchangeService.getBaseRate()

        client.execute(query) {
            base = it.payload
            lastRate = base!!.ref
            items.add(base!!)
            recyclerAdapter.notifyDataSetChanged()
            subscribeRates()
        }
    }

    private fun onUpdateRateFactor(newFactor: String) {
        val query = exchangeService.updateFactor(newFactor)

        client.execute(query) {
            getFactorEditText()?.error = null
            base?.value = newFactor
        }
    }

    private fun subscribeRates() {
        val query = exchangeService.rates()
        client.execute(query, SUBSCRIBE_REQUEST_CODE, ::onUpdateRates)
    }

    private fun onUpdateRates(items: ClientResponse<List<ExchangeService.RateResponse>>) {
        fun notBase(item: ExchangeService.RateResponse): Boolean {
            return item.ref != base?.ref
        }

        this.items.removeAll(::notBase)  // Удаляем из списка все, кроме базового

        val updatedItems = items.payload
            .filter(::notBase)

        this.items.addAll(updatedItems)
        recyclerAdapter.notifyItemRangeChanged(1, updatedItems.size)

        getFactorEditText()?.removeTextChangedListener(textWatcher)
        getFactorEditText()?.addTextChangedListener(textWatcher)
    }

    private fun onRateClick(rate: ExchangeService.RateResponse) {
        if (lastRate == rate.ref || rate.ref == base?.ref)
            return

        lastRate = rate.ref
        val query = exchangeService.setBase(rate.ref)

        client.execute(query) {
            base = rate.copy(value = "1")
            swapBaseRateItem()
        }
    }

    private fun swapBaseRateItem() {
        val index = items.indexOfFirst { it.ref == base?.ref }

        if (index == 0)
            return

        items.removeAt(index)
        items.add(0, base!!)

        recyclerAdapter.notifyItemMoved(index, 0)
        recyclerAdapter.notifyItemChanged(0)

        recyclerView.run {
            getFactorEditText()?.requestFocus()
            refreshRates()
        }
    }

    private fun getFactorEditText(): EditText? {
        val holder = recyclerView.findViewHolderForAdapterPosition(0) as? RateHolder
        return holder?.itemView?.rateEditText
    }

    private fun refreshRates() {
        val query = exchangeService.refreshRates()
        client.execute(query)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onRetryButtonClick(view: View) {
        noInternetErrorIncludeView.hideView()
        timeIsOutErrorIncludeView.hideView()

        subscribe()
    }

    override fun onValidationErrors(error: ClientResponse<ValidationErrors>) {
        val factorError = error.payload.errors["factor"] ?: return
        getFactorEditText()?.error = factorError.toString(this)
    }

    override fun onAccessError(error: ClientResponse<AccessError>) {
        when (error.payload.reason) {
            AccessErrorReason.LOST_CONNECTION -> noInternetErrorIncludeView.showView()
            AccessErrorReason.TIMEOUT -> timeIsOutErrorIncludeView.showView()
        }
    }

    override fun onRequestStateUpdated(requestState: ClientResponse<RequestState>) {
        if (requestState.requestCode != SUBSCRIBE_REQUEST_CODE)
            return

        if (items.size > 1) {
            progressBar.hideView()
            return
        }

        when (requestState.payload) {
            RequestState.STARTED -> progressBar.showView()
            RequestState.NEXT -> progressBar.hideView()
            RequestState.FINISHED -> progressBar.hideView()
        }
    }
}
