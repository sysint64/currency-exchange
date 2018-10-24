package ru.kabylin.andrey.currencyexchange.views.watchers

import android.text.Editable
import android.text.TextWatcher
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class TextWatcherChanged : TextWatcher {
    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
}

class StringTextWatcher(private var str: String) : TextWatcher {
    override fun afterTextChanged(s: Editable?) {}

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        str = s.toString()
    }
}

inline fun inputTextWatch(crossinline onChange: (newValue: String) -> Unit): ReadOnlyProperty<Any?, TextWatcherChanged> =
    object : ReadOnlyProperty<Any?, TextWatcherChanged> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): TextWatcherChanged =
            object : TextWatcherChanged() {
                override fun onTextChanged(sequence: CharSequence, start: Int, before: Int, count: Int) {
                    onChange(sequence.toString())
                }
            }
    }
