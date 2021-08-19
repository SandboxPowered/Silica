package org.sandboxpowered.silica.registry

import org.jetbrains.annotations.ApiStatus
import org.sandboxpowered.silica.util.Identifier
import java.util.stream.Stream

interface Registry<T : RegistryEntry<T>> : Iterable<T> {
    fun stream(): Stream<T>
    operator fun contains(id: Identifier): Boolean
    operator fun get(id: Identifier): RegistryObject<T>
    fun getId(element: T): Identifier

    @ApiStatus.Experimental
    fun getUnsafe(id: Identifier): T?
    val registryType: Class<T>
}