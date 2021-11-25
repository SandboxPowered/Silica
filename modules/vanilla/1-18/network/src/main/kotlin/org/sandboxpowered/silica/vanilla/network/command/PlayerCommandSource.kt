package org.sandboxpowered.silica.vanilla.network.command

import akka.actor.typed.ActorRef
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import org.sandboxpowered.silica.api.entity.Player
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.VanillaNetworkAdapter
import org.sandboxpowered.silica.vanilla.network.packets.play.clientbound.S2CChatMessage

class PlayerCommandSource(val packetHandler: PacketHandler, val context: PlayContext) : Player {
    override val world: ActorRef<World.Command> = context.world

    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        context.network.tell(
            VanillaNetworkAdapter.VanillaCommand.SendToAll(
                S2CChatMessage(
                    message,
                    0,
                    packetHandler.connection.profile.id
                )
            )
        )
    }
}