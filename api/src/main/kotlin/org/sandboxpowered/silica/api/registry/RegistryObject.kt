package org.sandboxpowered.silica.api.registry

import org.sandboxpowered.silica.api.util.Identifier
import java.util.*
import java.util.function.Supplier
import kotlin.reflect.KProperty

interface RegistryObject<T : RegistryEntry<T>> : Supplier<T> {
    fun asOptional(): Optional<T>

    @Throws(NoSuchElementException::class)
    override fun get(): T

    val isPresent: Boolean

    val isEmpty: Boolean

    fun or(supplier: RegistryObject<T>): RegistryObject<T>

    val id: Identifier

    fun orNull(): T?

    fun orElseGet(supplier: Supplier<T>): T

    fun <X : Throwable> orElseThrow(supplier: Supplier<X>): T

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = orNull()

    val guaranteed
        get() = NonnullObjectDelegate(this)

    val registry: Registry<T>

    class NonnullObjectDelegate<T : RegistryEntry<T>>(private val obj: RegistryObject<T>) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T = obj.get()
    }
}