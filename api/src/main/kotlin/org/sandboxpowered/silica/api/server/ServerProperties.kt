package org.sandboxpowered.silica.api.server

interface ServerProperties {
    val onlineMode: Boolean
    val motd: String
    val serverPort: Int
    val serverIp: String
    val maxTickTime: Int
    val maxPlayers: Int
    val supportChatFormatting: Boolean
}