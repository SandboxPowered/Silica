package org.sandboxpowered.silica.vanilla.network

import com.google.gson.*
import com.mojang.authlib.GameProfile
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.sandboxpowered.silica.api.util.extensions.plusAssign
import org.sandboxpowered.silica.api.util.extensions.set
import java.lang.reflect.Type

data class MOTD(
    val version: Version,
    val players: Players,
    var description: Component,
    var favicon: String
) {
    fun addPlayer(player: GameProfile) {
        if (players.sample.add(player)) players.online++
    }

    fun removePlayer(player: GameProfile) {
        if (players.sample.remove(player)) players.online--
    }
}

data class Players(
    var max: Int,
    var online: Int,
    var sample: MutableList<GameProfile>
)

data class Version(
    var name: String,
    var protocol: Int
)

class MOTDDeserializer : JsonDeserializer<MOTD> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): MOTD {
        TODO("Not yet implemented")
    }
}

class MOTDSerializer : JsonSerializer<MOTD> {
    override fun serialize(src: MOTD, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val obj = JsonObject()
        obj["version"] = JsonObject().apply {
            this["name"] = src.version.name
            this["protocol"] = src.version.protocol
        }
        obj["players"] = JsonObject().apply {
            this["max"] = src.players.max
            this["online"] = src.players.online
            this["sample"] = JsonArray().apply {
                src.players.sample.asSequence().take(20).forEach {
                    this += JsonObject().apply {
                        this["name"] = it.name
                        this["id"] = it.id.toString()
                    }
                }
                if (src.players.sample.size > 20) this += JsonObject().apply {
                    this["name"] = "..."
                    this["id"] = "..."
                }
            }
        }
        obj["description"] = GsonComponentSerializer.gson().serializeToTree(src.description)
        obj["favicon"] = src.favicon
        return obj
    }
}