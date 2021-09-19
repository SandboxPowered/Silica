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
            add(0x00, ::HandshakeRequest)
        }
    }),
    PLAY(0, {
        server {
            add(0x00, ::TeleportConfirmation)
            add(0x05, ::ClientSettings)
            add(0x0A, ::ClientPluginChannel)
            add(0x0F, ::KeepAliveServer)
            add(0x11, ::PlayerPosition)
            add(0x12, ::PlayerPositionAndRotation)
            add(0x13, ::PlayerRotation)
            add(0x14, ::PlayerMovement)
            add(0x1A, ::PlayerDigging)
            add(0x2C, ::HandSwingAnimation)
            add(0x1B, ::EntityAction)
            add(0x2E, ::PlayerBlockPlacement)
        }
        client {
            add(0x26, ::JoinGame)
            add(0x48, ::HeldItemChange)
            add(0x65, ::DeclareRecipes)
            add(0X66, ::DeclareTags)
            add(0x1B, ::EntityStatus)
            add(0x12, ::DeclareCommands)
            add(0x38, ::SetPlayerPositionAndLook)
            add(0x39, ::UnlockRecipes)
            add(0x36, ::PlayerInfo)
            add(0x49, ::UpdateChunkPosition)
            add(0x22, ::ChunkData)
            add(0x25, ::UpdateLight)
            add(0x20, ::WorldBorder)
            add(0x21, ::KeepAliveClient)
            add(0x08, ::AcknowledgePlayerDigging)
        }
    }),
    STATUS(1, {
        server {
            add(0x00, ::StatusRequest)
            add(0x01, ::PingRequest)
        }
        client {
            add(0x00, ::StatusResponse)
            add(0x01, ::PongResponse)
        }
    }),
    LOGIN(2, {
        server {
            add(0x00, ::LoginStart)
            add(0x01, ::EncryptionResponse)
        }
        client {
            add(0x00, ::Disconnect)
            add(0x01, ::EncryptionRequest)
            add(0x02, ::LoginSuccess)
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

            inline fun <reified P : PacketBase> add(targetId: Int, packetSupplier: Supplier<P>): FlowBuilder {
                val id = classToId.put(P::class.java, targetId)
                require(id == -1) { "Packet ${P::class.java} is already registered to ID $id" }
                idToConstructor[targetId] = packetSupplier
                return this
            }
        }

        val client = FlowBuilder(NetworkFlow.CLIENTBOUND)
        val server = FlowBuilder(NetworkFlow.SERVERBOUND)

        fun client(block: FlowBuilder.() -> Unit): FlowBuilder = client.apply(block)
        fun server(block: FlowBuilder.() -> Unit): FlowBuilder = server.apply(block)
    }
}

private operator fun <T> Int2ObjectMap<T>.contains(packetId: Int): Boolean = containsKey(packetId)
