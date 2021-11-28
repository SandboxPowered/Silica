package org.sandboxpowered.silica.world.persistence

import org.sandboxpowered.silica.api.world.state.block.BlockState

interface BlockStateMapping {
    operator fun get(state: BlockState): Int

    operator fun get(id: Int): BlockState
}