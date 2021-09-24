package org.sandboxpowered.silica.content.block

import com.artemis.ArchetypeBuilder
import org.sandboxpowered.silica.content.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.content.block.BlockProperties.LIT
import org.sandboxpowered.silica.ecs.component.FurnaceLogicComponent
import org.sandboxpowered.silica.ecs.component.ResizableInventoryComponent
import org.sandboxpowered.silica.ecs.system.FurnaceProcessingSystem
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.extensions.add
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

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