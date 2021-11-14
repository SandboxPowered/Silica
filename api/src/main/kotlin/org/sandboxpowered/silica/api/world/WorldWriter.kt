package org.sandboxpowered.silica.api.world

import com.artemis.EntityEdit
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.state.block.BlockState


interface WorldWriter {
    fun setBlockState(pos: Position, state: BlockState): Boolean = setBlockState(pos, state, Flag.DEFAULT)

    fun setBlockState(pos: Position, state: BlockState, flag: Flag): Boolean

    fun spawnEntity(entity: EntityDefinition, editor: (EntityEdit) -> Unit)

    @JvmInline
    value class Flag(val flag: Int) {
        companion object {
            /**
             * Sends a neighbor update event to surrounding blocks.
             */
            val NOTIFY_NEIGHBORS = Flag(1)

            /**
             * Notifies listeners and clients who need to react when the block changes.
             */
            val NOTIFY_LISTENERS = Flag(2)

            /**
             * Used in conjunction with [NOTIFY_LISTENERS] to suppress the render pass on clients.
             */
            val NO_REDRAW = Flag(4)

            /**
             * Forces a synchronous redraw on clients. Unused on silica client
             */
            val REDRAW_ON_MAIN_THREAD = Flag(8)

            /**
             * Bypass virtual block state changes and forces the passed state to be stored as-is.
             */
            val FORCE_STATE = Flag(16)

            /**
             * Prevents the previous block (container) from dropping items when destroyed.
             */
            val SKIP_DROPS = Flag(32)

            /**
             * Signals that the current block is being moved to a different location, usually because of a piston.
             */
            val MOVED = Flag(64)

            /**
             * Signals that lighting updates should be skipped.
             */
            val SKIP_LIGHTING_UPDATES = Flag(128)

            /**
             * The default setBlockState behavior. Same as [NOTIFY_NEIGHBORS] and [NOTIFY_LISTENERS]
             */
            val DEFAULT = NOTIFY_NEIGHBORS or NOTIFY_LISTENERS
        }

        private infix fun or(value: Flag): Flag = Flag(this.flag or value.flag)
        private infix fun not(value: Flag): Flag = Flag(this.flag and value.flag.inv())
        operator fun contains(value: Flag): Boolean = flag and value.flag == value.flag
    }
}