package org.sandboxpowered.silica.network

import org.sandboxpowered.silica.component.VanillaPlayerInput
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.world.World
import java.util.function.Consumer

typealias PlayerMutation = (VanillaPlayerInput) -> Unit
typealias WorldMutation = (World) -> Unit

class PlayContext(
    val server: SilicaServer,
    val mutatePlayer: (PlayerMutation) -> Unit,
    val mutateWorld: (WorldMutation) -> Unit
) {
    fun mutatePlayerJava(block: Consumer<VanillaPlayerInput>): Unit = this.mutatePlayer { block.accept(it) }
}