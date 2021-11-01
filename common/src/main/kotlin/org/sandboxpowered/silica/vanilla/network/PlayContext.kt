package org.sandboxpowered.silica.vanilla.network

import org.sandboxpowered.silica.content.inventory.PlayerInventory
import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput

typealias PlayerInventoryMutation = (PlayerInventory) -> Unit
typealias PlayerMutation = (VanillaPlayerInput) -> Unit

class PlayContext(
    val idToItem: (Int) -> Item,
    val mutatePlayerInventory: (PlayerInventoryMutation) -> Unit,
    val mutatePlayer: (PlayerMutation) -> Unit
)