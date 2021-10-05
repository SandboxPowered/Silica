package org.sandboxpowered.silica.server

interface ServerProperties {
    val onlineMode: Boolean
    val motd: String
    val serverPort: Int
    val serverIp: String
    val maxTickTime: Int
    val maxPlayers: Int
}