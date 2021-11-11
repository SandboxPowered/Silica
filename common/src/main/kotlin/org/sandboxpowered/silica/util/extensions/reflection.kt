package org.sandboxpowered.silica.util.extensions

import org.reflections.Reflections

inline fun <reified T : Annotation> Reflections.getTypesAnnotatedWith(): MutableSet<Class<*>> =
    getTypesAnnotatedWith(T::class.java)

inline fun <reified T : Annotation> Class<*>.getAnnotation(): T = getAnnotation(T::class.java)

inline fun <reified T> Class<*>.isAssignableFrom(): Boolean = isAssignableFrom(T::class.java)