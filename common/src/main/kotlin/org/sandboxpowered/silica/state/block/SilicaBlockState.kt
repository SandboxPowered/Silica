package org.sandboxpowered.silica.state.block

import com.google.common.collect.ImmutableMap
import org.sandboxpowered.api.block.Block
import org.sandboxpowered.api.world.state.BlockState
import org.sandboxpowered.api.world.state.Property
import org.sandboxpowered.silica.state.BaseState

class SilicaBlockState(base: Block, properties: ImmutableMap<Property<*>, Comparable<*>>) :
    BaseState<Block, BlockState>(base, properties), BlockState {
    override fun getBlock(): Block {
        return base
    }
}