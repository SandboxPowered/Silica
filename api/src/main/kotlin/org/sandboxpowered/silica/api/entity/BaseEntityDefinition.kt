package org.sandboxpowered.silica.api.entity

import com.artemis.ArchetypeBuilder
import com.artemis.Component
import org.sandboxpowered.silica.api.util.Identifier

class BaseEntityDefinition(
    override val identifier: Identifier,
    private vararg val components: Class<out Component>
) : EntityDefinition {

    override fun createArchetype(): ArchetypeBuilder = ArchetypeBuilder().add(*components)
}