package ru.kabylin.andrey.currencyexchange.models

import android.content.Context

interface ContextStringify {
    fun toString(context: Context): String
}
