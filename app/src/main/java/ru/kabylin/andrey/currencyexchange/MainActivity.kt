package ru.kabylin.andrey.currencyexchange

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_currency.view.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import org.kodein.di.generic.kcontext
import ru.kabylin.andrey.currencyexchange.client.Client
import ru.kabylin.andrey.currencyexchange.client.ClientErrorsListener
import ru.kabylin.andrey.currencyexchange.client.ClientResponse
import ru.kabylin.andrey.currencyexchange.client.ValidationErrors
import ru.kabylin.andrey.currencyexchange.client.view.ClientViewMediator
import ru.kabylin.andrey.currencyexchange.holders.RateHolder
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import ru.kabylin.andrey.currencyexchange.views.ViewMediatorAware
import ru.kabylin.andrey.currencyexchange.views.recyclerview.SingleItemRecyclerAdapter

class MainActivity : AppCompatActivity(), KodeinAware, ViewMediatorAware, ClientErrorsListener {
    override val kodeinContext = kcontext(this)
    override val kodein by closestKodein()

    private val client: Client by instance()
    private val exchangeService: ExchangeService by instance()

    override val viewMediator: ClientViewMediator by lazy {
        ClientViewMediator(client, this, lifecycle)
    }

    data class RateRecyclerItemBox(
        val response: ExchangeService.RateResponse,
        val isBase: Boolean,
        val onUpdateRate: ((String) -> Unit)?
    )

    private val items = ArrayList<RateRecyclerItemBox>()
    private var base: RateRecyclerItemBox? = null
    private var lastRate = ""

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
    }

    override fun subscribe() {
        items.clear()
        val query = exchangeService.getBaseRate()

        client.execute(query) {
            base = box(it.payload, isBase = true)
            lastRate = base!!.response.ref
            items.add(base!!)
            recyclerAdapter.notifyItemInserted(0)
            subscribeRates()
        }
    }

    private fun box(rate: ExchangeService.RateResponse, isBase: Boolean = false): RateRecyclerItemBox =
        RateRecyclerItemBox(
            response = rate,
            isBase = isBase,
            onUpdateRate = if (isBase) { ::onUpdateRateFactor } else { null }
        )

    private fun onUpdateRateFactor(newFactor: String) {
        val query = exchangeService.updateFactor(newFactor)

        client.execute(query) {
            getFactorEditText().error = null
        }
    }

    private fun subscribeRates() {
        val query = exchangeService.rates()
        client.execute(query, ::onUpdateRates)
    }

    private fun onUpdateRates(items: ClientResponse<List<ExchangeService.RateResponse>>) {
        fun notBase(item: RateRecyclerItemBox): Boolean {
            return item.response.ref != base?.response?.ref
        }

        this.items.removeAll(::notBase)  // Удаляем из списка все, кроме базового

        val updatedItems = items.payload
            .map { box(it) }
            .filter(::notBase)

        this.items.addAll(updatedItems)
        recyclerAdapter.notifyItemRangeChanged(1, updatedItems.size)
    }

    private fun onRateClick(rate: RateRecyclerItemBox) {
        if (lastRate == rate.response.ref || rate.response.ref == base?.response?.ref)
            return

        lastRate = rate.response.ref
        val query = exchangeService.setBase(rate.response.ref)

        client.execute(query) {
            base = rate.copy(response = rate.response.copy(value = "1"))
            swapBaseRateItem()
        }
    }

    private fun swapBaseRateItem() {
        val index = items.indexOfFirst { it.response.ref == base?.response?.ref }

        if (index == 0)
            return

        items.removeAt(index)
        items.add(0, base!!)

        recyclerAdapter.notifyItemMoved(index, 0)
        recyclerAdapter.notifyItemChanged(0)

        recyclerView.run {
            getFactorEditText().requestFocus()
            refreshRates()
        }
    }

    private fun getFactorEditText(): EditText {
        val view = recyclerView.findViewHolderForAdapterPosition(0).itemView
        return view.rateEditText
    }

    private fun refreshRates() {
        val query = exchangeService.refreshRates()
        client.execute(query)
    }

    override fun onValidationErrors(error: ClientResponse<ValidationErrors>) {
        val factorError = error.payload.errors["factor"] ?: return
        getFactorEditText().error = factorError.toString(this)
    }
}
