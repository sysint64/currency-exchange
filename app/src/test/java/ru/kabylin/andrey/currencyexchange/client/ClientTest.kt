package ru.kabylin.andrey.currencyexchange.client

import io.reactivex.*
import org.junit.Assert
import org.junit.Test

class ClientTest {
    private fun doSuccessTest(body: (Client) -> Unit) {
        val client = Client()
        var errors = 0

        client.errors.subscribe {
            it.payload.printStackTrace()
            errors++
        }

        body(client)
        Assert.assertEquals(0, errors)
    }

    @Test
    fun `should execute success Single query`() =
        doSuccessTest { client ->
            val query = Single.just(12)
            var completed = false

            client.execute(query) {
                Assert.assertEquals(Client.REQUEST_DEFAULT, it.requestCode)
                Assert.assertEquals(12, it.payload)
                completed = true
            }

            Assert.assertTrue(completed)
        }

    @Test
    fun `should execute success Flowable query`() =
        doSuccessTest { client ->
            val query = Flowable.just("Hello ", "world", "!")
            var result = ""
            var completed = false

            client.execute(query,
                onNext = {
                    Assert.assertEquals(Client.REQUEST_DEFAULT, it.requestCode)
                    result += it.payload
                },
                onComplete = {
                    completed = true
                }
            )

            Assert.assertTrue(completed)
            Assert.assertEquals("Hello world!", result)
        }

    @Test
    fun `should execute success Observable query`() =
        doSuccessTest { client ->
            val query = Observable.just("Hello ", "world", "!")
            var result = ""
            var completed = false

            client.execute(query,
                onNext = {
                    Assert.assertEquals(Client.REQUEST_DEFAULT, it.requestCode)
                    result += it.payload
                },
                onComplete = {
                    completed = true
                }
            )

            Assert.assertTrue(completed)
            Assert.assertEquals("Hello world!", result)
        }

    @Test
    fun `should execute success Completable query`() =
        doSuccessTest { client ->
            val query = Completable.complete()
            var completed = false

            client.execute(query) {
                completed = true
            }

            Assert.assertTrue(completed)
        }

    @Test
    fun `should execute success Single query with custom request code`() =
        doSuccessTest { client ->
            val query = Single.just(12)
            var completed = false

            client.execute(query, requestCode = 2002) {
                Assert.assertEquals(2002, it.requestCode)
                completed = true
            }

            Assert.assertTrue(completed)
        }

    @Test
    fun `should execute success Flowable query with custom request code`() =
        doSuccessTest { client ->
            val query = Flowable.just(1, 2, 3)
            var completed = false

            client.execute(query, requestCode = 2002,
                onNext = {
                    Assert.assertEquals(2002, it.requestCode)
                },
                onComplete = {
                    completed = true
                }
            )

            Assert.assertTrue(completed)
        }

    @Test
    fun `should execute success Observable query with custom request code`() =
        doSuccessTest { client ->
            val query = Observable.just(1, 2, 3)
            var completed = false

            client.execute(query, requestCode = 2002,
                onNext = {
                    Assert.assertEquals(2002, it.requestCode)
                },
                onComplete = {
                    completed = true
                }
            )

            Assert.assertTrue(completed)
        }

    @Test
    fun `should execute success Completable query with custom request code`() =
        doSuccessTest { client ->
            val query = Completable.complete()
            var completed = false

            client.execute(query, requestCode = 2002) {
                completed = true
            }

            Assert.assertTrue(completed)
        }

    private fun doErrorTest(requestCode: Int = Client.REQUEST_DEFAULT, body: (Client, Throwable) -> Unit) {
        val client = Client()
        var errors = 0

        client.errors.subscribe {
            it.payload.printStackTrace()

            Assert.assertEquals(requestCode, it.requestCode)
            Assert.assertTrue(it.payload is LogicError)
            Assert.assertEquals("Test error", (it.payload as LogicError).reason.toString())

            errors++
        }

        body(client, LogicError("Test error"))
        Assert.assertEquals(1, errors)
    }

    @Test
    fun `should execute emit error for Single query`() =
        doErrorTest { client, logicError ->
            val query = Single.error<Unit>(logicError)
            var completed = false

            client.execute(query) {
                completed = true
            }

            Assert.assertFalse(completed)
        }

    @Test
    fun `should execute emit error for Flowable query`() =
        doErrorTest { client, logicError ->
            val query = Flowable.create<Int>({ emitter ->
                emitter.onNext(1)
                emitter.onError(logicError)
            }, BackpressureStrategy.DROP)

            var completed = false
            var gotItem = false

            client.execute(query,
                onNext = {
                    Assert.assertEquals(Client.REQUEST_DEFAULT, it.requestCode)
                    gotItem = true
                },
                onComplete = {
                    completed = true
                }
            )

            Assert.assertTrue(gotItem)
            Assert.assertFalse(completed)
        }

    @Test
    fun `should execute emit error for Observable query`() =
        doErrorTest { client, logicError ->
            val query = Observable.create<Int> { emitter ->
                emitter.onNext(1)
                emitter.onError(logicError)
            }

            var completed = false
            var gotItem = false

            client.execute(query,
                onNext = {
                    Assert.assertEquals(Client.REQUEST_DEFAULT, it.requestCode)
                    gotItem = true
                },
                onComplete = {
                    completed = true
                }
            )

            Assert.assertTrue(gotItem)
            Assert.assertFalse(completed)
        }

    @Test
    fun `should execute emit error for Completable query`() =
        doErrorTest { client, logicError ->
            val query = Completable.error(logicError)
            var completed = false

            client.execute(query) {
                completed = true
            }

            Assert.assertFalse(completed)
        }

    @Test
    fun `should execute emit error for Single query with custom request code`() =
        doErrorTest(2002) { client, logicError ->
            val query = Single.error<Unit>(logicError)
            var completed = false

            client.execute(query, requestCode = 2002) {
                completed = true
            }

            Assert.assertFalse(completed)
        }

    @Test
    fun `should execute emit error for Flowable query with custom request code`() =
        doErrorTest(2002) { client, logicError ->
            val query = Flowable.create<Int>({ emitter ->
                emitter.onNext(1)
                emitter.onError(logicError)
            }, BackpressureStrategy.DROP)

            var completed = false
            var gotItem = false

            client.execute(query, requestCode = 2002,
                onNext = {
                    Assert.assertEquals(2002, it.requestCode)
                    gotItem = true
                },
                onComplete = {
                    completed = true
                }
            )

            Assert.assertTrue(gotItem)
            Assert.assertFalse(completed)
        }

    @Test
    fun `should execute emit error for Observable query with custom request code`() =
        doErrorTest(2002) { client, logicError ->
            val query = Observable.create<Int> { emitter ->
                emitter.onNext(1)
                emitter.onError(logicError)
            }

            var completed = false
            var gotItem = false

            client.execute(query, requestCode = 2002,
                onNext = {
                    Assert.assertEquals(2002, it.requestCode)
                    gotItem = true
                },
                onComplete = {
                    completed = true
                }
            )

            Assert.assertTrue(gotItem)
            Assert.assertFalse(completed)
        }

    @Test
    fun `should execute emit error for Completable query with custom request code`() =
        doErrorTest(2002) { client, logicError ->
            val query = Completable.error(logicError)
            var completed = false

            client.execute(query, requestCode = 2002) {
                completed = true
            }

            Assert.assertFalse(completed)
        }
}
