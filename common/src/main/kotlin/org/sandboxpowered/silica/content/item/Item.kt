package org.sandboxpowered.silica.content.item

import org.sandboxpowered.silica.registry.RegistryEntry

sealed interface Item : RegistryEntry<Item> {
    val properties: Properties

    class Properties(val fuelTime: Int, val maxStackSize: Int) {
        companion object {
            inline fun create(block: Builder.() -> Unit): Properties = Builder().apply(block).build()
        }

        class Builder {
            var fuelTime = -1
            var maxStackSize = 64

            fun build(): Properties = Properties(fuelTime, maxStackSize)
        }
    }
}