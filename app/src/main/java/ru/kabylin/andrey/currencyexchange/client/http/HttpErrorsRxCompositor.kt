package ru.kabylin.andrey.currencyexchange.client.http

import com.google.gson.JsonSyntaxException
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.HttpException
import ru.kabylin.andrey.currencyexchange.client.*
import ru.kabylin.andrey.currencyexchange.reactivex.compositors.RxCompositor
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Данный класс преобразует исключения [HttpException]
 * в понятные приложению исключения из client/errors.kt
 */
class HttpErrorsRxCompositor : RxCompositor {
    override fun <T> compose(single: Single<T>): Single<T> {
        return single.onErrorResumeNext { throwable: Throwable ->
            Single.error(remapExceptions(throwable))
        }
    }

    override fun <T> compose(flowable: Flowable<T>): Flowable<T> {
        return flowable.onErrorResumeNext { throwable: Throwable ->
            Flowable.error(remapExceptions(throwable))
        }
    }

    override fun <T> compose(observable: Observable<T>): Observable<T> {
        return observable.onErrorResumeNext { throwable: Throwable ->
            Observable.error(remapExceptions(throwable))
        }
    }

    override fun compose(completable: Completable): Completable {
        return completable.onErrorResumeNext { throwable: Throwable ->
            Completable.error(remapExceptions(throwable))
        }
    }

    companion object {
        fun remapExceptions(throwable: Throwable): Throwable =
            when (throwable) {
                is ConnectException -> AccessError(AccessErrorReason.LOST_CONNECTION, throwable)
                is SocketException -> AccessError(AccessErrorReason.LOST_CONNECTION, throwable)
                is UnknownHostException -> AccessError(AccessErrorReason.LOST_CONNECTION, throwable)
                is SocketTimeoutException -> AccessError(AccessErrorReason.TIMEOUT, throwable)
                is JsonSyntaxException -> AccessError(AccessErrorReason.BAD_RESPONSE, throwable)
                is HttpException -> parseHttpError(throwable)
                else -> throwable
            }

        private fun parseHttpError(httpException: HttpException): Throwable {
            val response = httpException.response()
            val statusCode = response.code()

            return when (statusCode) {
                400 -> AccessError(AccessErrorReason.BAD_RESPONSE, httpException)
                404 -> AccessError(AccessErrorReason.NOT_FOUND, httpException)
                401 -> SessionError("Unauthorized", httpException)
                403 -> CredentialsError("Доступ запрещен", httpException)
                405 -> AccessError("Access error: Method not allowed", httpException)
                423 -> AccessError("Access error: Locked", httpException)
                429 -> AccessError(AccessErrorReason.TOO_MANY_REQUESTS, httpException)
                500 -> AccessError(AccessErrorReason.INTERNAL_SERVER_ERROR, httpException)
                else -> AccessError(AccessErrorReason.BAD_RESPONSE, httpException)
            }
        }
    }
}

