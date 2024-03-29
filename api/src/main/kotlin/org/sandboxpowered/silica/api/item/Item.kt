package org.sandboxpowered.silica.api.item

import net.kyori.adventure.text.Component
import net.kyori.adventure.translation.Translatable
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry

interface Item : RegistryEntry<Item>, Translatable {
    val properties: Properties

    override fun translationKey(): String = "item.${identifier.namespace}.${identifier.path}"

    fun getDisplayName(): Component {
        return Component.translatable(this)
    }

    override val registry: Registry<Item> get() = Registries.ITEMS

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