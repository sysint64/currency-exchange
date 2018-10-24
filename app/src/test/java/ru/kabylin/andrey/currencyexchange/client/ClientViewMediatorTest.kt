package ru.kabylin.andrey.currencyexchange.client

import io.mockk.mockk
import junit.framework.Assert
import org.junit.Test
import ru.kabylin.andrey.currencyexchange.client.view.ClientViewMediator
import ru.kabylin.andrey.currencyexchange.views.ViewMediatorAware

class ClientViewMediatorTest {
    @Test
    fun `should subscribe on request state listener`() {
        val client = Client()
        var testRequestState: RequestState? = null

        class Aware : ViewMediatorAware, RequestStateListener {
            override val viewMediator = ClientViewMediator(client, this, mockk(relaxed = true))

            override fun onRequestStateUpdated(requestState: ClientResponse<RequestState>) {
                testRequestState = requestState.payload
            }
        }

        val aware = Aware()
        aware.viewMediator.subscribe()

        client.requestState.onNext(ClientResponse(RequestState.STARTED, success = true))
        Assert.assertEquals(RequestState.STARTED, testRequestState)

        client.requestState.onNext(ClientResponse(RequestState.FINISHED, success = true))
        Assert.assertEquals(RequestState.FINISHED, testRequestState)
    }

    @Test
    fun `should subscribe on error listener`() {
        val client = Client()

        var testError: Throwable? = null
        var testLogicError: LogicError? = null
        var testValidationError: ValidationErrors? = null
        var testCredentialsError: CredentialsError? = null
        var testAccessError: AccessError? = null
        var testCriticalError: Throwable? = null

        class Aware : ViewMediatorAware, ClientErrorsListener {
            override val viewMediator = ClientViewMediator(client, this, mockk(relaxed = true))

            override fun onLogicError(error: ClientResponse<LogicError>) {
                testLogicError = error.payload
            }

            override fun onValidationErrors(error: ClientResponse<ValidationErrors>) {
                testValidationError = error.payload
            }

            override fun onCredentialsError(error: ClientResponse<CredentialsError>) {
                testCredentialsError = error.payload
            }

            override fun onAccessError(error: ClientResponse<AccessError>) {
                testAccessError = error.payload
            }

            override fun onCriticalError(error: ClientResponse<Throwable>) {
                testCriticalError = error.payload
            }

            override fun onError(error: ClientResponse<Throwable>) {
                testError = error.payload
            }
        }

        val aware = Aware()
        aware.viewMediator.subscribe()

        fun assertError(expected: Throwable, actual: () -> Throwable?) {
            client.onError(expected)

            Assert.assertEquals(expected, actual())
            Assert.assertEquals(expected, testError)
        }

        assertError(LogicError("test")) { testLogicError }
        assertError(ValidationErrors("field", "error")) { testValidationError }
        assertError(CredentialsError("permission denied")) { testCredentialsError }
        assertError(AccessError(AccessErrorReason.LOST_CONNECTION)) { testAccessError }
        assertError(RuntimeException("Oh no!")) { testCriticalError }
    }
}
