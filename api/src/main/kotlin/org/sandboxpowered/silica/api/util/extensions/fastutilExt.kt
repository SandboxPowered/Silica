package org.sandboxpowered.silica.api.util.extensions

import it.unimi.dsi.fastutil.ints.*
import it.unimi.dsi.fastutil.objects.Object2IntFunction

operator fun Int2BooleanFunction.set(index: Int, value: Boolean) {
    this.put(index, value)
}

operator fun Int2ByteFunction.set(index: Int, value: Byte) {
    this.put(index, value)
}

operator fun Int2CharFunction.set(index: Int, value: Char) {
    this.put(index, value)
}

operator fun Int2DoubleFunction.set(index: Int, value: Double) {
    this.put(index, value)
}

operator fun Int2FloatFunction.set(index: Int, value: Float) {
    this.put(index, value)
}

operator fun Int2IntFunction.set(index: Int, value: Int) {
    this.put(index, value)
}

operator fun Int2LongFunction.set(index: Int, value: Long) {
    this.put(index, value)
}

operator fun <V> Int2ObjectFunction<V>.set(index: Int, value: V) {
    this.put(index, value)
}

operator fun <V> Object2IntFunction<V>.set(key: V, value: Int) {
    this.put(key, value)
}

operator fun Int2ShortFunction.set(index: Int, value: Short) {
    this.put(index, value)
}

operator fun <T> Int2ObjectMap<T>.contains(packetId: Int): Boolean = containsKey(packetId)

operator fun Int2BooleanMap.Entry.component1() = this.intKey
operator fun Int2BooleanMap.Entry.component2() = this.booleanValue

operator fun Int2ByteMap.Entry.component1() = this.intKey
operator fun Int2ByteMap.Entry.component2() = this.byteValue

operator fun Int2CharMap.Entry.component1() = this.intKey
operator fun Int2CharMap.Entry.component2() = this.charValue

operator fun Int2DoubleMap.Entry.component1() = this.intKey
operator fun Int2DoubleMap.Entry.component2() = this.doubleValue

operator fun Int2FloatMap.Entry.component1() = this.intKey
operator fun Int2FloatMap.Entry.component2() = this.floatValue

operator fun Int2IntMap.Entry.component1() = this.intKey
operator fun Int2IntMap.Entry.component2() = this.intValue

operator fun Int2LongMap.Entry.component1() = this.intKey
operator fun Int2LongMap.Entry.component2() = this.longValue

operator fun <V> Int2ObjectMap.Entry<V>.component1() = this.intKey
operator fun <V> Int2ObjectMap.Entry<V>.component2(): V? = this.value

operator fun Int2ShortMap.Entry.component1() = this.intKey
operator fun Int2ShortMap.Entry.component2() = this.shortValue
