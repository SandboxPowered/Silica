package org.sandboxpowered.silica.world.state.property

import java.util.*

sealed interface Property<T : Comparable<T>> {
    val propertyName: String
    val valueType: Class<T>
    val values: Collection<T>
    fun getValueString(value: T): String
    fun getValue(name: String): Optional<T>
}