package org.sandboxpowered.silica.api.server

import com.mojang.authlib.GameProfile
import net.kyori.adventure.text.Component
import org.sandboxpowered.silica.api.event.Event
import org.sandboxpowered.silica.api.event.EventFactory
import org.sandboxpowered.silica.api.event.EventResult
import org.sandboxpowered.silica.api.event.TypedEventResult
import org.sandboxpowered.silica.api.server.ServerEvents.ChatEvent
import org.sandboxpowered.silica.api.util.Identifier

object ServerEvents {
    // TODO: Completely rewrite this event to be infinitely more flexible.
    val CHAT_EVENT: Event<ChatEvent> = EventFactory.createEvent { handlers ->
        ChatEvent { player, channel, message ->
            var result = TypedEventResult(EventResult.DEFAULT, message)
            for (handler in handlers) {
                result = handler(player, channel, result.value)
                if (result.isCancelled) return@ChatEvent result
            }
            return@ChatEvent result
        }
    }

    fun interface ChatEvent {
        operator fun invoke(player: GameProfile, channel: Identifier, message: Component): TypedEventResult<Component>
    }
}