package org.sandboxpowered.silica.util.extensions

import joptsimple.ArgumentAcceptingOptionSpec
import kotlin.reflect.KClass

fun <V, T : Any> ArgumentAcceptingOptionSpec<V>.ofType(kClass: KClass<T>): ArgumentAcceptingOptionSpec<T> =
    ofType(kClass.java)

inline fun <reified T> ArgumentAcceptingOptionSpec<*>.ofType(): ArgumentAcceptingOptionSpec<T> {
    return ofType(T::class.java)
}
