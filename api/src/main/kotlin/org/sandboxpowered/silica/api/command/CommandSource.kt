package org.sandboxpowered.silica.api.command

import akka.actor.typed.ActorRef
import net.kyori.adventure.audience.Audience
import org.sandboxpowered.silica.api.world.World

interface CommandSource : Audience {
    val world: ActorRef<World.Command>
}