package org.sandboxpowered.silica.content.block

import com.artemis.ArchetypeBuilder
import com.artemis.BaseEntitySystem

interface BlockEntityProvider : Block {
    fun createArchetype(): ArchetypeBuilder

    fun createProcessingSystem(): BaseEntitySystem
}