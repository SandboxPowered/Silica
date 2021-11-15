package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.markdown.DiscordFlavor
import org.sandboxpowered.silica.api.event.EventResult
import org.sandboxpowered.silica.api.event.TypedEventResult
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.server.ServerEvents
import org.sandboxpowered.silica.api.util.EasterEggs
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.VanillaNetworkAdapter
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.packets.play.clientbound.S2CChatMessage

data class C2SChatMessage(private val message: String) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readString(MAX_SIZE))

    override fun write(buf: PacketBuffer) {
        buf.writeString(message.take(MAX_SIZE))
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        val profile = packetHandler.connection.profile
        if (message.startsWith('/')) {
            ServerEvents.CHAT_COMMAND_EVENT.dispatcher?.invoke(
                profile,
                Identifier("minecraft", "chat"),
                message.substring(1),
                context.world
            )
        } else {
            val formatter = MiniMessage.withMarkdownFlavor(DiscordFlavor.get())
            val username = if (profile.id.toString() in EasterEggs.DEV_UUID)
                formatter.parse("<rainbow><${profile.name}></rainbow>")
            else Component.text("<${profile.name}>")
            val text =
                if (context.properties.supportChatFormatting) formatter.parse(message) else Component.text(message)
            val message = username.append(" ").append(text)
            val result = ServerEvents.CHAT_EVENT.dispatcher?.invoke(
                profile,
                Identifier("minecraft", "chat"),
                message,
                context.world
            ) ?: TypedEventResult(EventResult.DEFAULT, message)
            if (!result.isCancelled) {
                context.network.tell(
                    VanillaNetworkAdapter.VanillaCommand.SendToAll(
                        S2CChatMessage(
                            result.value,
                            0,
                            packetHandler.connection.profile.id
                        )
                    )
                )
            }
        }
    }

    private companion object {
        private const val MAX_SIZE = 256
    }
}

private fun Component.append(s: String): Component {
    return append(Component.text(s))
}
