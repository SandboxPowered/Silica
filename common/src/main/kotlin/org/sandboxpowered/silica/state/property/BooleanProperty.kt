package org.sandboxpowered.silica.state.property

import com.google.common.collect.ImmutableSet
import java.util.*

class BooleanProperty private constructor(name: String) : AbstractProperty<Boolean>(name, Boolean::class) {
    override val values: ImmutableSet<Boolean> = ImmutableSet.of(true, false)

    override fun getValueString(value: Boolean): String {
        return value.toString()
    }

    override fun getValue(name: String): Optional<Boolean> =
        if ("true" != name && "false" != name) Optional.empty()
        else Optional.of(name == "true")

    companion object {
        @JvmStatic
        fun of(string: String): BooleanProperty = BooleanProperty(string)
    }
}