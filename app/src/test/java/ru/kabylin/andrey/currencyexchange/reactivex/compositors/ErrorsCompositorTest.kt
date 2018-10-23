package ru.kabylin.andrey.currencyexchange.reactivex.compositors

import com.rubylichtenstein.rxtest.assertions.shouldHave
import com.rubylichtenstein.rxtest.extentions.test
import io.reactivex.Single
import org.junit.Test
import ru.kabylin.andrey.currencyexchange.client.Client
import ru.kabylin.andrey.currencyexchange.client.LogicError
import com.rubylichtenstein.rxtest.matchers.error
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import org.junit.Assert

class ErrorsCompositorTest {
    private fun doTest(body: (ErrorsRxCompositor, Throwable) -> Unit) {
        val client = Client()
        val compositor = ErrorsRxCompositor(client)
        val errors = ArrayList<Throwable>()

        client.errors.subscribe {
            errors.add(it!!.payload)
        }

        val logicError = LogicError("Test error")

        body(compositor, logicError)

        Assert.assertEquals(1, errors.size)
        Assert.assertEquals("Test error", (errors[0] as LogicError).reason.toString())
    }

    @Test
    fun `should get error for Single`() =
        doTest { compositor, throwable ->
            compositor.compose(Single.error<Unit>(throwable))
                .test { it shouldHave error(throwable.javaClass) }
        }

    @Test
    fun `should get error for Flowable`() =
        doTest { compositor, throwable ->
            compositor.compose(Flowable.error<Unit>(throwable))
                .test { it shouldHave error(throwable.javaClass) }
        }

    @Test
    fun `should get error for Observable`() =
        doTest { compositor, throwable ->
            compositor.compose(Observable.error<Unit>(throwable))
                .test { it shouldHave error(throwable.javaClass) }
        }

    @Test
    fun `should get error for Completable`() =
        doTest { compositor, throwable ->
            compositor.compose(Completable.error(throwable))
                .test { it shouldHave error(throwable.javaClass) }
        }
}
