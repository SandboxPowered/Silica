package org.sandboxpowered.silica.api.command.sync

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.ArgumentType
import org.sandboxpowered.silica.api.network.PacketBuffer

class SingletonArgumentSerializer<T : ArgumentType<*>>(val constructor: () -> T) : ArgumentTypes.ArgumentSerializer<T> {
    override fun serializeToNetwork(argumentType: T, buffer: PacketBuffer) {}

    override fun deserializeFromNetwork(buffer: PacketBuffer): T = constructor()

    override fun serializeToJson(argumentType: T, jsonObject: JsonObject) {}
}