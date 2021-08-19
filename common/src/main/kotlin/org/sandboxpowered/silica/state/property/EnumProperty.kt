package org.sandboxpowered.silica.state.property

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

class EnumProperty<E> private constructor(name: String, type: Class<E>, collection: Collection<E>) :
    AbstractProperty<E>(name, type) where E : Enum<E>, E : StringSerializable {
    override val values: ImmutableSet<E>
    private val names: MutableMap<String, E> = HashMap()
    override fun getValueString(value: E): String = value.getName()

    override fun getValue(name: String): Optional<E> = Optional.ofNullable(names[name])

    companion object {
        @JvmStatic
        fun <T> of(name: String, type: Class<T>): EnumProperty<T> where T : Enum<T>, T : StringSerializable {
            return of(name, type) { true }
        }

        @JvmStatic
        fun <T> of(
            name: String,
            type: Class<T>,
            predicate: Predicate<T>?
        ): EnumProperty<T> where T : Enum<T>, T : StringSerializable {
            return of(name, type, Arrays.stream(type.enumConstants).filter(predicate).collect(Collectors.toList()))
        }

        @JvmStatic
        fun <T> of(
            name: String,
            type: Class<T>,
            vararg values: T
        ): EnumProperty<T> where T : Enum<T>, T : StringSerializable {
            return of(name, type, Lists.newArrayList(*values))
        }

        @JvmStatic
        fun <T> of(
            name: String,
            type: Class<T>,
            values: Collection<T>
        ): EnumProperty<T> where T : Enum<T>, T : StringSerializable {
            return EnumProperty(name, type, values)
        }
    }

    init {
        values = ImmutableSet.copyOf(collection)
        values.forEach(Consumer { e: E -> names[getValueString(e)] = e })
    }
}