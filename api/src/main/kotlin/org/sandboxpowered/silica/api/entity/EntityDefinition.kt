package org.sandboxpowered.silica.api.entity

import com.artemis.ArchetypeBuilder
import net.kyori.adventure.translation.Translatable
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry

interface EntityDefinition : RegistryEntry<EntityDefinition>, Translatable {

    override fun translationKey(): String = "entity.${identifier.namespace}.${identifier.path}"

    fun createArchetype(): ArchetypeBuilder

    override val registry: Registry<EntityDefinition> get() = Registries.ENTITY_DEFINITIONS
}