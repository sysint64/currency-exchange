package ru.kabylin.andrey.currencyexchange.client.http

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.kabylin.andrey.currencyexchange.BuildConfig
import ru.kabylin.andrey.currencyexchange.client.Client
import java.util.concurrent.TimeUnit

class HttpClient : Client() {
    private val mainApiRetrofit by lazy {
        val endpoint = BuildConfig.API_ENDPOINT
        retrofitBuilder(endpoint)
            .client(createHttpClientInstance())
            .build()
    }

    val gson = Gson()

    private fun retrofitBuilder(endpoint: String): Retrofit.Builder {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(endpoint)
    }

    private fun createHttpClientInstance(): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.level = HttpLoggingInterceptor.Level.BODY
            httpClient.addInterceptor(logging)
        }

        return httpClient.build()
    }

    enum class Dest {
        MAIN_API,
        ;
    }

    fun <T> createRetrofitGateway(cls: Class<T>, dest: Dest = Dest.MAIN_API): T {
        return when (dest) {
            Dest.MAIN_API -> mainApiRetrofit.create(cls)
        }
    }
}
