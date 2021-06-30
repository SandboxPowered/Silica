package org.sandboxpowered.silica.engine

import org.sandboxpowered.api.client.Client
import org.sandboxpowered.api.engine.FactoryProvider
import org.sandboxpowered.api.engine.Game
import org.sandboxpowered.api.engine.Platform
import org.sandboxpowered.api.network.PacketBuffer
import org.sandboxpowered.api.server.Server
import java.nio.file.Path
import java.util.function.Function

class SilicaGame(private var factoryProvider: FactoryProvider) : Game {
    override fun getGameDirectory(): Path {
        TODO("Not yet implemented")
    }

    override fun getPlatform(): Platform {
        TODO("Not yet implemented")
    }

    override fun isServerAvailable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getServer(): Server {
        TODO("Not yet implemented")
    }

    override fun isClientAvailable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getClient(): Client {
        TODO("Not yet implemented")
    }

    override fun getCompatibilityMode(): Game.CompatibilityMode = Game.CompatibilityMode.SILICA

    override fun getFactoryProvider(): FactoryProvider = factoryProvider

    override fun <T : Any?> registerPacket(type: Class<T>, reader: Function<PacketBuffer, T>, side: Platform.Type) {
        TODO("Not yet implemented")
    }
}