package org.sandboxpowered.silica.api.command.sync

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.FloatArgumentType
import org.sandboxpowered.silica.api.command.sync.ArgumentTypes.createNumberFlags
import org.sandboxpowered.silica.api.network.PacketBuffer

class FloatArgumentSerializer : ArgumentTypes.ArgumentSerializer<FloatArgumentType> {
    override fun serializeToNetwork(argumentType: FloatArgumentType, buffer: PacketBuffer) {
        val bl = argumentType.minimum != Float.MIN_VALUE
        val bl2 = argumentType.maximum != Float.MAX_VALUE
        buffer.writeByte(createNumberFlags(bl, bl2))
        if (bl) buffer.writeFloat(argumentType.minimum)
        if (bl2) buffer.writeFloat(argumentType.maximum)
    }

    override fun deserializeFromNetwork(buffer: PacketBuffer): FloatArgumentType {
        TODO("Not yet implemented")
    }

    override fun serializeToJson(argumentType: FloatArgumentType, jsonObject: JsonObject) {
        TODO("Not yet implemented")
    }
}