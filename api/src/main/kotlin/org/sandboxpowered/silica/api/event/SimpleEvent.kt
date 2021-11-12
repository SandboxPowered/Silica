package org.sandboxpowered.silica.api.event

class SimpleEvent<T>(private val type: Class<T>, private val invokerFactory: (List<T>) -> T) : Event<T> {
    @Volatile
    private var _invoker: T? = null

    override val invoker: T?
        get() = if (handlers.isNotEmpty()) _invoker ?: recreateInvoker() else null

    init {
        recreateInvoker()
    }

    private var handlers: List<T> = mutableListOf()

    fun recreateInvoker(): T {
        _invoker = invokerFactory(handlers)
        return _invoker!!
    }

    override fun subscribe(listener: T) {
        synchronized(this) {
            handlers = handlers + listener
            recreateInvoker()
        }
    }
}