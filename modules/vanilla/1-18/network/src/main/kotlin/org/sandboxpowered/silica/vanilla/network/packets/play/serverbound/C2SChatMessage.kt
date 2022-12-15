package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.sandboxpowered.silica.api.SilicaAPI
import org.sandboxpowered.silica.api.event.EventResult
import org.sandboxpowered.silica.api.event.TypedEventResult
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.server.ServerEvents
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.VanillaNetworkAdapter
import org.sandboxpowered.silica.vanilla.network.command.PlayerCommandSource
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
            val dispatcher = SilicaAPI.commandDispatcher
            val cs = PlayerCommandSource(packetHandler, context)
            try {
                val res = dispatcher.execute(message.substring(1), cs)
                if (res == 0) {
                    cs.sendMessage(Component.text("Got 0").color(NamedTextColor.RED))
                }
            } catch (e: CommandSyntaxException) {
                if (e.type == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand()) {
                    cs.sendMessage(Component.text("Command not found.").color(NamedTextColor.RED))
                } else {
                    cs.sendMessage(Component.text("ERROR: ${e.message}").color(NamedTextColor.RED))
                }
            }
        } else {
            // MiniMessage doesn't support MD anymore
//            val format = MiniMessage.withMarkdownFlavor(DiscordFlavor.get())
            val format = MiniMessage.miniMessage()
            val username = Component.text("<${profile.name}>")
            // TODO : check if it's safe to use MiniMessage to parse & format the message
            val text =
                if (context.properties.supportChatFormatting) format.deserialize(message) else Component.text(message)
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
