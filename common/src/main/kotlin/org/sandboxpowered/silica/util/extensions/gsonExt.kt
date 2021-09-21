package org.sandboxpowered.silica.util.extensions

import com.google.gson.Gson

inline fun <reified T> Gson.fromJson(s: String): T {
    return fromJson(s, T::class.java)
}