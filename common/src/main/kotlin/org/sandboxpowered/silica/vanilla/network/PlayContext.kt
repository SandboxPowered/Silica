package org.sandboxpowered.silica.vanilla.network

import org.sandboxpowered.silica.content.inventory.PlayerInventory
import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput
import org.sandboxpowered.silica.registry.RegistryObject

typealias PlayerInventoryMutation = (PlayerInventory) -> Unit
typealias PlayerMutation = (VanillaPlayerInput) -> Unit

class PlayContext(
    val idToItem: (Int) -> RegistryObject<Item>,
    val mutatePlayerInventory: (PlayerInventoryMutation) -> Unit,
    val mutatePlayer: (PlayerMutation) -> Unit
)