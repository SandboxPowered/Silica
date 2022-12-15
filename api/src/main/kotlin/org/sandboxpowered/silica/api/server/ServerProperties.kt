package org.sandboxpowered.silica.api.server

interface ServerProperties {
    // Vanilla Properties
    val onlineMode: Boolean
    val motd: String
    val serverPort: Int
    val serverIp: String
    val maxTickTime: Int
    val maxPlayers: Int

    // Silica-specific Properties
    val supportChatFormatting: Boolean

    // Velocity Integration
    val velocityEnabled: Boolean
    val velocityKey: String
}