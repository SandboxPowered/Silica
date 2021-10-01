package org.sandboxpowered.silica.util.extensions

import com.google.gson.*

inline fun <reified T> GsonBuilder.registerTypeAdapter(deserializer: JsonDeserializer<T>): GsonBuilder =
    registerTypeAdapter(T::class.java, deserializer)

inline fun <reified T> JsonDeserializationContext.deserialize(element: JsonElement): T =
    deserialize(element, T::class.java)

inline fun <reified T> Gson.fromJson(s: String): T = fromJson(s, T::class.java)

fun JsonObject.getNullable(key: String): JsonElement? = if (has(key)) get(key) else null

fun JsonObject.getString(key: String, default: String): String = getNullable(key)?.asString ?: default

fun JsonObject.getInt(key: String, default: Int): Int = getNullable(key)?.asInt ?: default

fun JsonObject.getFloat(key: String, default: Float): Float = getNullable(key)?.asFloat ?: default

fun JsonObject.getBoolean(key: String, default: Boolean): Boolean = getNullable(key)?.asBoolean ?: default