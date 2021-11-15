package org.sandboxpowered.silica.api.command.sync

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.DoubleArgumentType
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.command.sync.ArgumentTypes.createNumberFlags

class DoubleArgumentSerializer : ArgumentTypes.ArgumentSerializer<DoubleArgumentType> {
    override fun serializeToNetwork(argumentType: DoubleArgumentType, buffer: PacketBuffer) {
        val bl = argumentType.minimum != Double.MIN_VALUE
        val bl2 = argumentType.maximum != Double.MAX_VALUE
        buffer.writeByte(createNumberFlags(bl, bl2))
        if (bl) buffer.writeDouble(argumentType.minimum)
        if (bl2) buffer.writeDouble(argumentType.maximum)
    }

    override fun deserializeFromNetwork(buffer: PacketBuffer): DoubleArgumentType {
        TODO("Not yet implemented")
    }

    override fun serializeToJson(argumentType: DoubleArgumentType, jsonObject: JsonObject) {
        TODO("Not yet implemented")
    }
}