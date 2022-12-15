package org.sandboxpowered.silica.vanilla.block

import com.artemis.ArchetypeBuilder
import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.block.BlockEntityProvider
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.extensions.add
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.vanilla.block.BlockProperties.LIT
import org.sandboxpowered.silica.vanilla.ecs.FurnaceLogicComponent
import org.sandboxpowered.silica.vanilla.ecs.FurnaceProcessingSystem
import org.sandboxpowered.silica.vanilla.ecs.ResizableInventoryComponent

class FurnaceBlock(identifier: Identifier) : BaseBlock(identifier), BlockEntityProvider {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(HORIZONTAL_FACING, LIT)
    }

    override fun createArchetype() = ArchetypeBuilder().apply {
        add<FurnaceLogicComponent>()
        add<ResizableInventoryComponent>()
    }

    override fun createProcessingSystem() = FurnaceProcessingSystem()
}