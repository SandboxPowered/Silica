package org.sandboxpowered.silica.server

import com.google.gson.annotations.SerializedName

data class MOTD(
    @SerializedName("version") val version: Version,
    @SerializedName("players") val players: Players,
    @SerializedName("description") val description: Description,
    @SerializedName("favicon") var favicon: String
) {
    fun addPlayer(player: String) {
        if (players.sample.add(player)) players.online++
    }

    fun removePlayer(player: String) {
        if (players.sample.remove(player)) players.online--
    }
}

data class Description(
    @SerializedName("text") var text: String
)

data class Players(
    @SerializedName("max") var max: Int,
    @SerializedName("online") var online: Int,
    @SerializedName("sample") var sample: MutableList<String>
)

data class Version(
    @SerializedName("name") var name: String,
    @SerializedName("protocol") var protocol: Int
)