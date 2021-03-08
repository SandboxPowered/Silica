package org.sandboxpowered.silica.server

import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class ServerEula(private val path: Path) {
    val agreed = checkAgreement()

    private fun checkAgreement(): Boolean {
        if (Files.notExists(path)) writeDefaultEULA()
        val properties = Properties()
        Files.newInputStream(path).use {
            properties.load(it)
        }
        return properties.getProperty("eula", "false").toBoolean()
    }

    private fun writeDefaultEULA() {
        Files.createFile(path)
        Files.newOutputStream(path).use {
            val properties = Properties()
            properties.setProperty("eula", "false")
            properties.store(
                it,
                "By changing the setting below to true you are indicating your agreement to the Minecraft End User License Agreement (https://account.mojang.com/documents/minecraft_eula)."
            )
        }
    }
}