package org.sandboxpowered.silica.block

import com.artemis.ArchetypeBuilder
import org.sandboxpowered.silica.component.BlockPositionComponent
import org.sandboxpowered.silica.component.FurnaceLogicComponent
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.extensions.add

class FurnaceBlock(identifier: Identifier) : BaseBlock(identifier), BlockEntityProvider {
    override fun createBlockEntityArchetype(): ArchetypeBuilder {
        val builder = ArchetypeBuilder()

        builder.add<BlockPositionComponent>()
        builder.add<FurnaceLogicComponent>()

        return builder
    }
}