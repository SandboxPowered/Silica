package org.sandboxpowered.silica.world.state.property

import com.google.common.collect.ImmutableSet
import com.google.common.collect.Sets
import java.util.*

class IntProperty private constructor(name: String, min: Int, max: Int) : AbstractProperty<Int>(name, Int::class) {
    override val values: ImmutableSet<Int>
    override fun getValueString(value: Int): String = value.toString()

    override fun getValue(name: String): Optional<Int> {
        val i = Integer.valueOf(name)
        return if (values.contains(i)) {
            Optional.of(i)
        } else {
            Optional.empty()
        }
    }

    companion object {
        @JvmStatic
        fun of(name: String, min: Int, max: Int): IntProperty = IntProperty(name, min, max)
    }

    init {
        require(min >= 0) { "Min value of $name must be 0 or greater" }
        require(max > min) { "Max value of $name must be greater than min ($min)" }
        val set: MutableSet<Int> = Sets.newHashSet()
        for (`val` in min..max) {
            set.add(`val`)
        }
        values = ImmutableSet.copyOf(set)
    }
}