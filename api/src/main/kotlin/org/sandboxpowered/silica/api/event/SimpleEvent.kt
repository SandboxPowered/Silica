package org.sandboxpowered.silica.api.event

class SimpleEvent<T>(private val type: Class<T>, private val dispatchFactory: (List<T>) -> T) : Event<T> {
    @Volatile
    private var _dispatcher: T? = null

    override val dispatcher: T?
        get() = if (handlers.isNotEmpty()) _dispatcher ?: reconstructDispatcher() else null

    private val handlers: MutableList<T> = ArrayList()

    init {
        reconstructDispatcher()
    }

    fun reconstructDispatcher(): T {
        _dispatcher = dispatchFactory(handlers)
        return _dispatcher!!
    }

    override fun subscribe(listener: T) {
        synchronized(this) {
            handlers += listener
            reconstructDispatcher()
        }
    }
}