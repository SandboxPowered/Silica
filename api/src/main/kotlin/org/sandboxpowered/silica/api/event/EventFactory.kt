package org.sandboxpowered.silica.api.event

object EventFactory {
    private val EVENTS = ArrayList<SimpleEvent<*>>()

    fun invalidate() {
        EVENTS.forEach(SimpleEvent<*>::recreateInvoker)
    }

    inline fun <reified T> createEvent(noinline invoker: (List<T>) -> T): Event<T> {
        return createEvent(T::class.java, invoker)
    }

    fun <T> createEvent(type: Class<T>, invoker: (List<T>) -> T): Event<T> {
        val event = SimpleEvent(type, invoker)
        EVENTS += event
        return event
    }
}