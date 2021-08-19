package org.sandboxpowered.silica.state.property

import java.util.*

interface Property<T : Comparable<T>> {
    val propertyName: String
    val valueType: Class<T>
    val values: Collection<T>
    fun getValueString(value: T): String
    fun getValue(name: String): Optional<T>
}