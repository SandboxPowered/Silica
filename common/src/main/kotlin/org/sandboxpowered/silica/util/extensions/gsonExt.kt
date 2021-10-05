package org.sandboxpowered.silica.util.extensions

import com.google.gson.*

inline fun <reified T> GsonBuilder.registerTypeAdapter(deserializer: JsonDeserializer<T>): GsonBuilder =
    registerTypeAdapter(T::class.java, deserializer)

inline fun <reified T> GsonBuilder.registerTypeAdapter(serializer: JsonSerializer<T>): GsonBuilder =
    registerTypeAdapter(T::class.java, serializer)

inline fun <reified T> JsonDeserializationContext.deserialize(element: JsonElement): T =
    deserialize(element, T::class.java)

inline fun <reified T> Gson.fromJson(s: String): T = fromJson(s, T::class.java)

fun JsonObject.getNullable(key: String): JsonElement? = if (has(key)) get(key) else null

fun JsonObject.getString(key: String, default: String): String = getNullable(key)?.asString ?: default

fun JsonObject.getInt(key: String, default: Int): Int = getNullable(key)?.asInt ?: default

fun JsonObject.getFloat(key: String, default: Float): Float = getNullable(key)?.asFloat ?: default

fun JsonObject.getBoolean(key: String, default: Boolean): Boolean = getNullable(key)?.asBoolean ?: default

operator fun JsonArray.plusAssign(value: JsonElement) = add(value)

operator fun JsonArray.plusAssign(value: String) = add(value)

operator fun JsonArray.plusAssign(value: Number) = add(value)

operator fun JsonArray.plusAssign(value: Char) = add(value)

operator fun JsonObject.set(member: String, value: JsonElement) = add(member, value)

operator fun JsonObject.set(member: String, value: String) = addProperty(member, value)

operator fun JsonObject.set(member: String, value: Number) = addProperty(member, value)

operator fun JsonObject.set(member: String, value: Char) = addProperty(member, value)