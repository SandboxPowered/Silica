package org.sandboxpowered.silica.api.command.sync

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.StringArgumentType
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.network.writeEnum

class StringArgumentSerializer : ArgumentTypes.ArgumentSerializer<StringArgumentType> {
    override fun serializeToNetwork(argumentType: StringArgumentType, buffer: PacketBuffer) {
        buffer.writeEnum(argumentType.type)
    }

    override fun deserializeFromNetwork(buffer: PacketBuffer): StringArgumentType {
        TODO("Not yet implemented")
    }

    override fun serializeToJson(argumentType: StringArgumentType, jsonObject: JsonObject) {
        TODO("Not yet implemented")
    }
}