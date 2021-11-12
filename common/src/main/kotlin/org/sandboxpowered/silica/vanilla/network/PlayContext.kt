package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.item.inventory.PlayerInventory
import org.sandboxpowered.silica.api.registry.RegistryObject
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput
import org.sandboxpowered.silica.server.ServerProperties
import org.sandboxpowered.silica.server.VanillaNetwork
import org.sandboxpowered.silica.world.SilicaWorld

typealias PlayerInventoryMutation = (PlayerInventory) -> Unit
typealias PlayerMutation = (VanillaPlayerInput) -> Unit

class PlayContext(
    val idToItem: (Int) -> RegistryObject<Item>,
    val itemToId: (Item) -> Int,
    val mutatePlayerInventory: (PlayerInventoryMutation) -> Unit,
    val mutatePlayer: (PlayerMutation) -> Unit,
    val world: ActorRef<SilicaWorld.Command>,
    val network: ActorRef<VanillaNetwork>,
    val properties: ServerProperties
)