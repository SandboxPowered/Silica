package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.item.inventory.PlayerInventory
import org.sandboxpowered.silica.api.registry.RegistryObject
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput
import org.sandboxpowered.silica.world.SilicaWorld

typealias PlayerInventoryMutation = (PlayerInventory) -> Unit
typealias PlayerMutation = (VanillaPlayerInput) -> Unit

class PlayContext(
    val idToItem: (Int) -> RegistryObject<Item>,
    val mutatePlayerInventory: (PlayerInventoryMutation) -> Unit,
    val mutatePlayer: (PlayerMutation) -> Unit,
    val world: ActorRef<SilicaWorld.Command>
)