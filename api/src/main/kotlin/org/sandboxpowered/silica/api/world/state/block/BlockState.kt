package org.sandboxpowered.silica.api.world.state.block

import com.google.common.collect.ImmutableMap
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.world.state.BaseState
import org.sandboxpowered.silica.api.world.state.property.Property

class BlockState(base: Block, properties: ImmutableMap<Property<*>, Comparable<*>>) :
    BaseState<Block, BlockState>(base, properties) {
    val isAir: Boolean
        get() = this.block.isAir

    val block: Block
        get() = base

    companion object {
        val factory = ::BlockState
    }
}