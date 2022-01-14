package org.sandboxpowered.silica.api.entity

import com.artemis.ArchetypeBuilder
import org.sandboxpowered.utilities.Identifier

class BaseEntityDefinition(
    override val identifier: Identifier,
    private val builder: ArchetypeBuilder.() -> Unit
) : EntityDefinition {

    override fun createArchetype(): ArchetypeBuilder = ArchetypeBuilder().apply(builder)
}