package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.item.inventory.PlayerInventory
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.registry.RegistryObject
import org.sandboxpowered.silica.api.server.ServerProperties
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.vanilla.network.ecs.component.VanillaPlayerInputComponent

typealias PlayerInventoryMutation = (PlayerInventory) -> Unit
typealias PlayerMutation = (VanillaPlayerInputComponent) -> Unit

class PlayContext(
    val idToItem: (Int) -> RegistryObject<Item>,
    val itemToId: (Item) -> Int,
    val mutatePlayerInventory: (PlayerInventoryMutation) -> Unit,
    val mutatePlayer: (PlayerMutation) -> Unit,
    val world: ActorRef<World.Command>,
    val network: ActorRef<NetworkAdapter.Command>,
    val properties: ServerProperties
)