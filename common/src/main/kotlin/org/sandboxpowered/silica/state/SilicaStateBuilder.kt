package org.sandboxpowered.silica.state

import org.sandboxpowered.api.state.property.PropertyContainer
import com.google.common.collect.Maps
import org.sandboxpowered.api.content.Content
import org.sandboxpowered.api.state.StateFactory
import org.sandboxpowered.api.state.property.Property

class SilicaStateBuilder<B : Content<B>, S : PropertyContainer<S>?>(private val base: B) : StateFactory.Builder<B, S> {
    private val properties: MutableMap<String, Property<*>> = Maps.newHashMap()

    fun getProperties(): Map<String, Property<*>> {
        return properties
    }

    override fun add(vararg properties: Property<*>): StateFactory.Builder<B, S> {
        for (property in properties) {
            this.properties[property.name] = property
        }
        return this
    }
}