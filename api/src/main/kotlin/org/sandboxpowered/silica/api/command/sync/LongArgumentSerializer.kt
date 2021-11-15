package org.sandboxpowered.silica.api.command.sync

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.LongArgumentType
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.command.sync.ArgumentTypes.createNumberFlags

class LongArgumentSerializer : ArgumentTypes.ArgumentSerializer<LongArgumentType> {
    override fun serializeToNetwork(argumentType: LongArgumentType, buffer: PacketBuffer) {
        val bl = argumentType.minimum != Long.MIN_VALUE
        val bl2 = argumentType.maximum != Long.MAX_VALUE
        buffer.writeByte(createNumberFlags(bl, bl2))
        if (bl) buffer.writeLong(argumentType.minimum)
        if (bl2) buffer.writeLong(argumentType.maximum)
    }

    override fun deserializeFromNetwork(buffer: PacketBuffer): LongArgumentType {
        TODO("Not yet implemented")
    }

    override fun serializeToJson(argumentType: LongArgumentType, jsonObject: JsonObject) {
        TODO("Not yet implemented")
    }
}