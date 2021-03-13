package org.sandboxpowered.silica.network

import org.sandboxpowered.silica.component.VanillaPlayerInput
import java.util.function.Consumer

typealias PlayerMutation = (VanillaPlayerInput) -> Unit

class PlayContext(
    val mutatePlayer: (PlayerMutation) -> Unit
) {
    fun mutatePlayerJava(block: Consumer<VanillaPlayerInput>): Unit = this.mutatePlayer { block.accept(it) }
}