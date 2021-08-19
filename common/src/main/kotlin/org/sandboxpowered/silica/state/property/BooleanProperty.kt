package org.sandboxpowered.silica.state.property

import com.google.common.collect.ImmutableSet
import java.util.*

class BooleanProperty private constructor(name: String) : AbstractProperty<Boolean>(name, Boolean::class) {
    override val values = ImmutableSet.of(true, false)
    override fun getValueString(value: Boolean): String {
        return value.toString()
    }

    override fun getValue(name: String): Optional<Boolean> {
        return if ("true" != name && "false" != name) Optional.empty()
        else Optional.of(java.lang.Boolean.valueOf(name))
    }

    companion object {
        @JvmStatic
        fun of(string: String): BooleanProperty {
            return BooleanProperty(string)
        }
    }
}