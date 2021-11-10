package org.sandboxpowered.silica.api.world

import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.state.block.BlockState

interface WorldWriter {
    fun setBlockState(pos: Position, state: BlockState): Boolean = setBlockState(pos, state, *Flag.DEFAULT)

    fun setBlockState(pos: Position, state: BlockState, vararg flags: Flag): Boolean

    @Suppress("unused")
    enum class Flag(val flag: Int) {
        /**
         * Sends a neighbor update event to surrounding blocks.
         */
        NOTIFY_NEIGHBORS(1),

        /**
         * Notifies listeners and clients who need to react when the block changes.
         */
        NOTIFY_LISTENERS(2),

        /**
         * Used in conjunction with [NOTIFY_LISTENERS] to suppress the render pass on clients.
         */
        NO_REDRAW(4),

        /**
         * Forces a synchronous redraw on clients. Unused on silica client
         */
        REDRAW_ON_MAIN_THREAD(8),

        /**
         * Bypass virtual block state changes and forces the passed state to be stored as-is.
         */
        FORCE_STATE(16),

        /**
         * Prevents the previous block (container) from dropping items when destroyed.
         */
        SKIP_DROPS(32),

        /**
         * Signals that the current block is being moved to a different location, usually because of a piston.
         */
        MOVED(64),

        /**
         * Signals that lighting updates should be skipped.
         */
        SKIP_LIGHTING_UPDATES(128);

        companion object {
            /**
             * The default setBlockState behavior. Same as [NOTIFY_NEIGHBORS] and [NOTIFY_LISTENERS]
             */
            val DEFAULT = arrayOf(NOTIFY_NEIGHBORS, NOTIFY_LISTENERS)
        }
    }
}