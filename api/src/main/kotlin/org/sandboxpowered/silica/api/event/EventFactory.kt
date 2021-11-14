package org.sandboxpowered.silica.api.event

object EventFactory {
    private val EVENTS = ArrayList<SimpleEvent<*>>()

    fun invalidate() {
        EVENTS.forEach(SimpleEvent<*>::reconstructDispatcher)
    }

    inline fun <reified T> createEvent(noinline dispatchFactory: (List<T>) -> T): Event<T> {
        return createEvent(T::class.java, dispatchFactory)
    }

    fun <T> createEvent(type: Class<T>, dispatchFactory: (List<T>) -> T): Event<T> {
        val event = SimpleEvent(type, dispatchFactory)
        EVENTS += event
        return event
    }
}