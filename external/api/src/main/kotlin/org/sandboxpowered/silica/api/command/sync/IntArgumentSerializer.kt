package org.sandboxpowered.silica.api.command.sync

import com.google.gson.JsonObject
import com.mojang.brigadier.arguments.IntegerArgumentType
import org.sandboxpowered.silica.api.command.sync.ArgumentTypes.createNumberFlags
import org.sandboxpowered.silica.api.network.PacketBuffer

class IntArgumentSerializer : ArgumentTypes.ArgumentSerializer<IntegerArgumentType> {
    override fun serializeToNetwork(argumentType: IntegerArgumentType, buffer: PacketBuffer) {
        val bl = argumentType.minimum != Int.MIN_VALUE
        val bl2 = argumentType.maximum != Int.MAX_VALUE
        buffer.writeByte(createNumberFlags(bl, bl2))
        if (bl) buffer.writeInt(argumentType.minimum)
        if (bl2) buffer.writeInt(argumentType.maximum)
    }

    override fun deserializeFromNetwork(buffer: PacketBuffer): IntegerArgumentType {
        TODO("Not yet implemented")
    }

    override fun serializeToJson(argumentType: IntegerArgumentType, jsonObject: JsonObject) {
        TODO("Not yet implemented")
    }
}