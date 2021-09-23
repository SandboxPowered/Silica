package org.sandboxpowered.silica.vanilla.network

import com.google.common.collect.Iterables
import com.google.common.collect.Maps
import io.netty.util.AttributeKey
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.sandboxpowered.silica.util.Util.getLogger
import org.sandboxpowered.silica.vanilla.network.handshake.clientbound.PingRequest
import org.sandboxpowered.silica.vanilla.network.handshake.clientbound.StatusRequest
import org.sandboxpowered.silica.vanilla.network.handshake.serverbound.PongResponse
import org.sandboxpowered.silica.vanilla.network.handshake.serverbound.StatusResponse
import org.sandboxpowered.silica.vanilla.network.login.clientbound.Disconnect
import org.sandboxpowered.silica.vanilla.network.login.clientbound.EncryptionRequest
import org.sandboxpowered.silica.vanilla.network.login.clientbound.LoginSuccess
import org.sandboxpowered.silica.vanilla.network.login.serverbound.EncryptionResponse
import org.sandboxpowered.silica.vanilla.network.login.serverbound.HandshakeRequest
import org.sandboxpowered.silica.vanilla.network.login.serverbound.LoginStart
import org.sandboxpowered.silica.vanilla.network.play.clientbound.*
import org.sandboxpowered.silica.vanilla.network.play.serverbound.*
import java.util.function.Function
import java.util.function.Supplier
import kotlin.collections.set

@Suppress("unused")
enum class Protocol(private val id: Int, block: Builder.() -> Unit) {
    HANDSHAKE(-1, {
        server {
            0x00 packet ::HandshakeRequest
        }
    }),
    PLAY(0, {
        server {
            0x00 packet ::TeleportConfirmation
            0x05 packet ::ClientSettings
            0x0A packet ::ClientPluginChannel
            0x0F packet ::KeepAliveServer
            0x11 packet ::PlayerPosition
            0x12 packet ::PlayerPositionAndRotation
            0x13 packet ::PlayerRotation
            0x14 packet ::PlayerMovement
            0x1A packet ::PlayerDigging
            0x2C packetDeprecated ::HandSwingAnimation
            0x1B packetDeprecated ::EntityAction
            0x2E packetDeprecated ::PlayerBlockPlacement
            0x25 packetDeprecated ::HeldItemChangeServerbound
        }
        client {
            0x26 packet ::JoinGame
            0x48 packet ::HeldItemChangeClientbound
            0x65 packetDeprecated ::DeclareRecipes
            0X66 packetDeprecated ::DeclareTags
            0x1B packetDeprecated ::EntityStatus
            0x12 packetDeprecated ::DeclareCommands
            0x38 packetDeprecated ::SetPlayerPositionAndLook
            0x39 packetDeprecated ::UnlockRecipes
            0x36 packetDeprecated ::PlayerInfo
            0x49 packetDeprecated ::UpdateChunkPosition
            0x22 packetDeprecated ::ChunkData
            0x25 packetDeprecated ::UpdateLight
            0x20 packetDeprecated ::WorldBorder
            0x21 packet ::KeepAliveClient
            0x08 packet ::AcknowledgePlayerDigging
            0x0C packet ::BlockChange
            0x04 packetDeprecated ::SpawnPlayer
            0x29 packetDeprecated ::UpdateEntityPosition
            0x2A packetDeprecated ::UpdateEntityPositionRotation
            0x2B packetDeprecated ::UpdateEntityRotation
            0x14 packet ::InitWindowItems
        }
    }),
    STATUS(1, {
        server {
            0x00 packet ::StatusRequest
            0x01 packet ::PingRequest
        }
        client {
            0x00 packet ::StatusResponse
            0x01 packet ::PongResponse
        }
    }),
    LOGIN(2, {
        server {
            0x00 packet ::LoginStart
            0x01 packet ::EncryptionResponse
        }
        client {
            0x00 packet ::Disconnect
            0x01 packet ::EncryptionRequest
            0x02 packet ::LoginSuccess
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

    fun createPacket(networkFlow: NetworkFlow, packetId: Int, buf: PacketByteBuf): PacketBase? = when (networkFlow) {
        NetworkFlow.CLIENTBOUND -> builder.client.createPacket(packetId, buf)
        NetworkFlow.SERVERBOUND -> builder.server.createPacket(packetId, buf)
    }

    class Builder {
        class FlowBuilder(val flow: NetworkFlow) {
            val classToId: Object2IntMap<Class<out PacketBase>> = Object2IntOpenHashMap()
            val idToConstructor: Int2ObjectMap<Function<PacketByteBuf, out PacketBase>> = Int2ObjectOpenHashMap()

            init {
                classToId.defaultReturnValue(-1)
            }

            fun getId(aClass: Class<out PacketBase>): Int = classToId.getInt(aClass)

            val allPackets: Iterable<Class<out PacketBase>>
                get() = Iterables.unmodifiableIterable(classToId.keys)

            fun createPacket(packetId: Int, buf: PacketByteBuf): PacketBase? =
                if (!idToConstructor.containsKey(packetId)) null else idToConstructor[packetId].apply(buf)

            @Deprecated("use PacketByteBuf constructor instead")
            inline infix fun <reified P : PacketBase> Int.packetDeprecated(packetSupplier: Supplier<P>) {
                packet { buf ->
                    val packet = packetSupplier.get()
                    packet.read(buf)
                    packet
                }
            }

            inline infix fun <reified P : PacketBase> Int.packet(packetSupplier: Function<PacketByteBuf, P>) {
                val id = classToId.put(P::class.java, this)
                require(id == -1) { "Packet ${P::class.java} is already registered to ID $id" }
                idToConstructor[this] = packetSupplier
            }
        }

        val client = FlowBuilder(NetworkFlow.CLIENTBOUND)
        val server = FlowBuilder(NetworkFlow.SERVERBOUND)

        inline fun client(block: FlowBuilder.() -> Unit): FlowBuilder = client.apply(block)
        inline fun server(block: FlowBuilder.() -> Unit): FlowBuilder = server.apply(block)
    }
}