package org.sandboxpowered.silica.api.nbt

import org.jetbrains.annotations.Contract
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
@Contract(pure = true)
inline fun nbt(crossinline content: NBTCompound.() -> Unit): NBTCompound {
    contract {
        callsInPlace(content, InvocationKind.EXACTLY_ONCE)
    }
    return CompoundTag().apply(content)
}

@OptIn(ExperimentalContracts::class)
inline fun NBTWritableCompound.setTag(key: String, crossinline content: NBTCompound.() -> Unit) {
    contract {
        callsInPlace(content, InvocationKind.EXACTLY_ONCE)
    }
    setTag(key, nbt(content))
}
