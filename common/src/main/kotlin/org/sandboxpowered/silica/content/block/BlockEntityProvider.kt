package org.sandboxpowered.silica.content.block

import com.artemis.ArchetypeBuilder
import com.artemis.systems.IteratingSystem

interface BlockEntityProvider : Block {
    fun createArchetype(): ArchetypeBuilder

    fun createProcessingSystem(): IteratingSystem
}