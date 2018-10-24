package ru.kabylin.andrey.currencyexchange

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance
import org.kodein.di.generic.kcontext
import ru.kabylin.andrey.currencyexchange.client.Client
import ru.kabylin.andrey.currencyexchange.client.ClientResponse
import ru.kabylin.andrey.currencyexchange.client.view.ClientViewMediator
import ru.kabylin.andrey.currencyexchange.holders.RateHolder
import ru.kabylin.andrey.currencyexchange.services.ExchangeService
import ru.kabylin.andrey.currencyexchange.views.ViewMediatorAware
import ru.kabylin.andrey.currencyexchange.views.recyclerview.SingleItemRecyclerAdapter

class MainActivity : AppCompatActivity(), KodeinAware, ViewMediatorAware {
    override val kodeinContext = kcontext(this)
    override val kodein by closestKodein()

    private val client: Client by instance()
    private val exchangeService: ExchangeService by instance()

    override val viewMediator: ClientViewMediator by lazy {
        ClientViewMediator(client, this, lifecycle)
    }

    private val items = ArrayList<ExchangeService.RateResponse>()
    private var base: ExchangeService.RateResponse? = null

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
            base = it.payload
            items.add(it.payload)
            recyclerAdapter.notifyItemInserted(0)
            subscribeRates()
        }
    }

    private fun subscribeRates() {
        val query = exchangeService.rates()
        client.execute(query, ::onUpdateRates)
    }

    private fun onUpdateRates(items: ClientResponse<List<ExchangeService.RateResponse>>) {
        fun notBase(item: ExchangeService.RateResponse): Boolean {
            return item.ref != base?.ref
        }

        this.items.removeAll(::notBase)  // Удаляем из списка все, кроме базового

        val updatedItems = items.payload.filter(::notBase)
        this.items.addAll(updatedItems)

        recyclerAdapter.notifyItemRangeChanged(1, updatedItems.size)
    }

    private fun onRateClick(rate: ExchangeService.RateResponse) {
    }
}
