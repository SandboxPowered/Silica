package org.sandboxpowered.silica.network

import com.google.common.collect.Iterables
import com.google.common.collect.Maps
import io.netty.util.AttributeKey
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.sandboxpowered.silica.network.handshake.clientbound.PingRequest
import org.sandboxpowered.silica.network.handshake.clientbound.StatusRequest
import org.sandboxpowered.silica.network.handshake.serverbound.PongResponse
import org.sandboxpowered.silica.network.handshake.serverbound.StatusResponse
import org.sandboxpowered.silica.network.login.clientbound.Disconnect
import org.sandboxpowered.silica.network.login.clientbound.EncryptionRequest
import org.sandboxpowered.silica.network.login.clientbound.LoginSuccess
import org.sandboxpowered.silica.network.login.serverbound.EncryptionResponse
import org.sandboxpowered.silica.network.login.serverbound.HandshakeRequest
import org.sandboxpowered.silica.network.login.serverbound.LoginStart
import org.sandboxpowered.silica.network.play.clientbound.*
import org.sandboxpowered.silica.network.play.serverbound.*
import org.sandboxpowered.silica.util.Util.getLogger
import java.util.function.Supplier
import kotlin.collections.set

enum class Protocol(private val id: Int, block: Builder.() -> Unit) {
    HANDSHAKE(-1, {
        server {
            0x00 means ::HandshakeRequest
        }
    }),
    PLAY(0, {
        server {
            0x00 means ::TeleportConfirmation
            0x05 means ::ClientSettings
            0x0A means ::ClientPluginChannel
            0x0F means ::KeepAliveServer
            0x11 means ::PlayerPosition
            0x12 means ::PlayerPositionAndRotation
            0x13 means ::PlayerRotation
            0x14 means ::PlayerMovement
            0x1A means ::PlayerDigging
            0x2C means ::HandSwingAnimation
            0x1B means ::EntityAction
            0x2E means ::PlayerBlockPlacement
        }
        client {
            0x26 means ::JoinGame
            0x48 means ::HeldItemChange
            0x65 means ::DeclareRecipes
            0X66 means ::DeclareTags
            0x1B means ::EntityStatus
            0x12 means ::DeclareCommands
            0x38 means ::SetPlayerPositionAndLook
            0x39 means ::UnlockRecipes
            0x36 means ::PlayerInfo
            0x49 means ::UpdateChunkPosition
            0x22 means ::ChunkData
            0x25 means ::UpdateLight
            0x20 means ::WorldBorder
            0x21 means ::KeepAliveClient
            0x08 means ::AcknowledgePlayerDigging
        }
    }),
    STATUS(1, {
        server {
            0x00 means ::StatusRequest
            0x01 means ::PingRequest
        }
        client {
            0x00 means ::StatusResponse
            0x01 means ::PongResponse
        }
    }),
    LOGIN(2, {
        server {
            0x00 means ::LoginStart
            0x01 means ::EncryptionResponse
        }
        client {
            0x00 means ::Disconnect
            0x01 means ::EncryptionRequest
            0x02 means ::LoginSuccess
        }
    });

    private val builder: Builder

    init {
        builder = Builder()
        block(builder)
    }

    companion object {
        val logger = getLogger<Protocol>()

        @JvmField
        val PROTOCOL_ATTRIBUTE_KEY: AttributeKey<Protocol> = AttributeKey.valueOf("protocol")
        private val PROTOCOL_BY_PACKET: MutableMap<Class<out PacketBase>, Protocol> = Maps.newHashMap()
        private val ID_2_PROTOCOL: Int2ObjectMap<Protocol> = Int2ObjectOpenHashMap()

        @JvmStatic
        fun getProtocolForPacket(packet: PacketBase): Protocol =
            PROTOCOL_BY_PACKET[packet.javaClass] ?: error("No protocol found for ${packet.javaClass}")

        @JvmStatic
        fun getProtocolFromId(id: Int): Protocol = ID_2_PROTOCOL[id]

        init {
            for (protocol in values()) {
                protocol.builder.client.allPackets.forEach { PROTOCOL_BY_PACKET[it] = protocol }
                protocol.builder.server.allPackets.forEach { PROTOCOL_BY_PACKET[it] = protocol }
                ID_2_PROTOCOL[protocol.id] = protocol
            }
        }
    }

    fun getPacketId(networkFlow: NetworkFlow, msg: PacketBase): Int = when (networkFlow) {
        NetworkFlow.CLIENTBOUND -> builder.client.getId(msg.javaClass)
        NetworkFlow.SERVERBOUND -> builder.server.getId(msg.javaClass)
    }

    fun createPacket(networkFlow: NetworkFlow, packetId: Int): PacketBase? = when (networkFlow) {
        NetworkFlow.CLIENTBOUND -> builder.client.createPacket(packetId)
        NetworkFlow.SERVERBOUND -> builder.server.createPacket(packetId)
    }

    class Builder {
        class FlowBuilder(val flow: NetworkFlow) {
            val classToId: Object2IntMap<Class<out PacketBase>> = Object2IntOpenHashMap()
            val idToConstructor: Int2ObjectMap<Supplier<out PacketBase>> = Int2ObjectOpenHashMap()

            init {
                classToId.defaultReturnValue(-1)
            }

            fun getId(aClass: Class<out PacketBase>): Int = classToId.getInt(aClass)

            val allPackets: Iterable<Class<out PacketBase>>
                get() = Iterables.unmodifiableIterable(classToId.keys)

            fun createPacket(packetId: Int): PacketBase? =
                if (packetId !in idToConstructor) null else idToConstructor[packetId].get()

            inline infix fun <reified P : PacketBase> Int.means(packetSupplier: Supplier<P>): FlowBuilder {
                val id = classToId.put(P::class.java, this)
                require(id == -1) { "Packet ${P::class.java} is already registered to ID $id" }
                idToConstructor[this] = packetSupplier
                return this@FlowBuilder
            }
        }

        val client = FlowBuilder(NetworkFlow.CLIENTBOUND)
        val server = FlowBuilder(NetworkFlow.SERVERBOUND)

        fun client(block: FlowBuilder.() -> Unit): FlowBuilder = client.apply(block)
        fun server(block: FlowBuilder.() -> Unit): FlowBuilder = server.apply(block)
    }
}

private operator fun <T> Int2ObjectMap<T>.contains(packetId: Int): Boolean = containsKey(packetId)
