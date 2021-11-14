package org.sandboxpowered.silica.api.server

import akka.actor.typed.ActorRef
import com.mojang.authlib.minecraft.MinecraftSessionService
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.world.World

interface Server {
    val world: ActorRef<World.Command>
    val network: ActorRef<NetworkAdapter.Command>
    val properties: ServerProperties
    val sessionService: MinecraftSessionService
}