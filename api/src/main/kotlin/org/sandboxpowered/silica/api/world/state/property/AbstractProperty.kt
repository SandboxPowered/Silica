package org.sandboxpowered.silica.api.world.state.property

import kotlin.reflect.KClass

abstract class AbstractProperty<T : Comparable<T>> protected constructor(
    final override val propertyName: String,
    final override val valueType: Class<T>
) : Property<T> {

    protected constructor(propertyName: String, valueType: KClass<T>) : this(propertyName, valueType.java)

    override fun equals(other: Any?): Boolean {
        return if (this === other) {
            true
        } else if (other !is AbstractProperty<*>) {
            false
        } else {
            valueType == other.valueType && propertyName == other.propertyName
        }
    }

    override fun hashCode(): Int = 31 * valueType.hashCode() + propertyName.hashCode()
}