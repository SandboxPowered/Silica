package org.sandboxpowered.silica.vanilla.network.play.serverbound

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.markdown.DiscordFlavor
import org.sandboxpowered.silica.EasterEggs
import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.extensions.create
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.server.VanillaNetwork
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CChatMessage
import org.sandboxpowered.silica.world.SilicaWorld.Command.DelayedCommand.Companion.spawnEntity

data class C2SChatMessage(private val message: String) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readString(MAX_SIZE))

    override fun write(buf: PacketBuffer) {
        buf.writeString(message.take(MAX_SIZE))
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        if (message.startsWith("/spawn")) {
            //TODO: Create a proper command system
            val parts = message.split(' ')
            if (parts.size >= 5) {
                val entity = Registries.ENTITY_DEFINITIONS[Identifier(parts[1])].orNull()
                if (entity != null) context.world.tell(spawnEntity(entity) {
                    val pos = it.create<PositionComponent>().pos
                    pos.set(parts[2].toDouble() + .5, parts[3].toDouble(), parts[4].toDouble() + .5)
                })
            }
        } else {
            val profile = packetHandler.connection.profile
            val formatter = MiniMessage.withMarkdownFlavor(DiscordFlavor.get())
            //TODO: Allow plugins to modify chat message layout
            val username = if (profile.id.toString() in EasterEggs.DEV_UUID)
                formatter.parse("<rainbow><${profile.name}></rainbow>")
            else Component.text("<${profile.name}>")
            val text =
                if (context.properties.supportChatFormatting) formatter.parse(message) else Component.text(message)
            context.network.tell(
                VanillaNetwork.SendToAll(
                    S2CChatMessage(
                        username.append(" ").append(text),
                        0,
                        packetHandler.connection.profile.id
                    )
                )
            )
        }
    }

    private companion object {
        private const val MAX_SIZE = 256
    }
}

private fun Component.append(s: String): Component {
    return append(Component.text(s))
}
