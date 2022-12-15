package org.sandboxpowered.silica.api.world.state

import com.google.common.collect.Maps
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.world.state.property.Property

class SilicaStateBuilder<B : RegistryEntry<B>, S : PropertyContainer<S>>(private val base: B) :
    StateProvider.Builder<B, S> {
    private val properties: MutableMap<String, Property<*>> = Maps.newHashMap()

    fun getProperties(): Map<String, Property<*>> {
        return properties
    }

    override fun add(vararg properties: Property<*>): StateProvider.Builder<B, S> {
        for (property in properties) {
            this.properties[property.propertyName] = property
        }
        return this
    }
}