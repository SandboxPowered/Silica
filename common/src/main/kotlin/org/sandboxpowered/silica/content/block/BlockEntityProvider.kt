package org.sandboxpowered.silica.content.block

import com.artemis.ArchetypeBuilder

interface BlockEntityProvider {
    fun createArchetype(): ArchetypeBuilder
}