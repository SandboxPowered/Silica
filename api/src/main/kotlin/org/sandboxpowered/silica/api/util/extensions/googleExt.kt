package org.sandboxpowered.silica.api.util.extensions

import com.google.common.collect.Table

operator fun <R, C, V> Table<R, C, V>.set(row: R, column: C, value: V) {
    put(row, column, value)
}