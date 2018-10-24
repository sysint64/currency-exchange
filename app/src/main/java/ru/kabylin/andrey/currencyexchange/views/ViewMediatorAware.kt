package ru.kabylin.andrey.currencyexchange.views

interface ViewMediatorAware {
    val viewMediator: ViewMediator

    /**
     * Перерисовка всего интерфейса, используется в основнои только при
     * полном восстановлении состояния экрана.
     */
    fun viewStateRefresh() {}

    fun subscribe() {}

    fun unsubscribe() {}
}
