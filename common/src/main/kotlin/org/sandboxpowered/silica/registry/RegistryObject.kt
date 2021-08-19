package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.util.Identifier
import java.util.*
import java.util.function.Supplier

interface RegistryObject<T : RegistryEntry<T>> : Supplier<T> {
    fun asOptional(): Optional<T>

    @Throws(NoSuchElementException::class)
    override fun get(): T

    val isPresent: Boolean
    val isEmpty: Boolean
    fun or(supplier: RegistryObject<T>): RegistryObject<T>
    val id: Identifier
    fun orElseGet(supplier: Supplier<T>): T

    fun <X : Throwable> orElseThrow(supplier: Supplier<X>): T
    val registry: Registry<T>
}