package org.sandboxpowered.silica.vanilla.network

import org.sandboxpowered.silica.content.inventory.PlayerInventory
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.world.World

typealias PlayerInventoryMutation = (PlayerInventory) -> Unit
typealias PlayerMutation = (VanillaPlayerInput) -> Unit
typealias WorldMutation = (World) -> Unit

class PlayContext(
    val server: SilicaServer,
    val mutatePlayerInventory: (PlayerInventoryMutation) -> Unit,
    val mutatePlayer: (PlayerMutation) -> Unit,
    val mutateWorld: (WorldMutation) -> Unit
)