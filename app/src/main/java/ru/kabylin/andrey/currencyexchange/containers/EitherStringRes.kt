package ru.kabylin.andrey.currencyexchange.containers

import android.content.Context
import android.support.annotation.StringRes
import ru.kabylin.andrey.currencyexchange.models.ContextStringify
import java.lang.UnsupportedOperationException

class EitherStringRes(val string: String?, val res: Int?) : ContextStringify {
    init {
        assert((string == null && res != null) || (res == null && string != null))
    }

    companion object {
        fun string(string: String): EitherStringRes {
            return EitherStringRes(string, null)
        }

        fun res(@StringRes res: Int): EitherStringRes {
            return EitherStringRes(null, res)
        }
    }

    fun apply(string: (value: String) -> Unit, res: (value: Int) -> Unit) {
        when {
            this.string != null -> string(this.string)
            this.res != null -> res(this.res)
        }
    }

    override fun toString(): String {
        return this.string ?: super.toString()
    }

    override fun toString(context: Context): String =
        when {
            this.string != null -> this.string
            this.res != null -> context.getString(this.res)
            else -> throw UnsupportedOperationException()
        }
}
