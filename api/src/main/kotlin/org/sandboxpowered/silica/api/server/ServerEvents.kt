package org.sandboxpowered.silica.api.server

import akka.actor.typed.ActorRef
import com.mojang.authlib.GameProfile
import net.kyori.adventure.text.Component
import org.sandboxpowered.silica.api.event.Event
import org.sandboxpowered.silica.api.event.EventFactory
import org.sandboxpowered.silica.api.event.EventResult
import org.sandboxpowered.silica.api.event.TypedEventResult
import org.sandboxpowered.silica.api.server.ServerEvents.ChatEvent
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.World

object ServerEvents {
    // TODO: Completely rewrite this event to be infinitely more flexible.
    val CHAT_EVENT: Event<ChatEvent<Component>> = chatLikeEvent()
    val CHAT_COMMAND_EVENT: Event<ChatEvent<String>> = chatLikeEvent()

    private fun <T : Any> chatLikeEvent(): Event<ChatEvent<T>> = EventFactory.createEvent { handlers ->
        ChatEvent { player, channel, message, world ->
            var result = TypedEventResult(EventResult.DEFAULT, message)
            for (handler in handlers) {
                result = handler(player, channel, result.value, world)
                if (result.isCancelled) return@ChatEvent result
            }
            return@ChatEvent result
        }
    }

    fun interface ChatEvent<T : Any> {
        operator fun invoke(
            player: GameProfile,
            channel: Identifier,
            message: T,
            world: ActorRef<in World.Command>
        ): TypedEventResult<T>
    }
}