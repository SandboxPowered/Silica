package org.sandboxpowered.silica.vanilla.network

import org.sandboxpowered.silica.content.inventory.PlayerInventory
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput

typealias PlayerInventoryMutation = (PlayerInventory) -> Unit
typealias PlayerMutation = (VanillaPlayerInput) -> Unit

class PlayContext(
    val mutatePlayerInventory: (PlayerInventoryMutation) -> Unit,
    val mutatePlayer: (PlayerMutation) -> Unit
)