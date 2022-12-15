package org.sandboxpowered.silica.api.block

import com.artemis.ArchetypeBuilder
import com.artemis.BaseEntitySystem

interface BlockEntityProvider : Block {
    fun createArchetype(): ArchetypeBuilder

    fun createProcessingSystem(): BaseEntitySystem? = null

    val processingSystemPriority: Int
        get() = 0
}