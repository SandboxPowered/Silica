package org.sandboxpowered.silica.block

import com.artemis.ArchetypeBuilder

interface BlockEntityProvider {
    fun createBlockEntityArchetype(): ArchetypeBuilder
}