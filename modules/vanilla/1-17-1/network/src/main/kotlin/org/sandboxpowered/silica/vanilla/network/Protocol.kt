package org.sandboxpowered.silica.vanilla.network

import com.google.common.collect.Iterables
import com.google.common.collect.Maps
import io.netty.util.AttributeKey
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.sandboxpowered.silica.api.network.Packet
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.packets.PacketBase
import org.sandboxpowered.silica.vanilla.network.packets.handshake.clientbound.C2SPingRequest
import org.sandboxpowered.silica.vanilla.network.packets.handshake.clientbound.C2SStatusRequest
import org.sandboxpowered.silica.vanilla.network.packets.handshake.serverbound.S2CPongResponse
import org.sandboxpowered.silica.vanilla.network.packets.handshake.serverbound.S2CStatusResponse
import org.sandboxpowered.silica.vanilla.network.packets.login.clientbound.S2CDisconnect
import org.sandboxpowered.silica.vanilla.network.packets.login.clientbound.S2CEncryptionRequest
import org.sandboxpowered.silica.vanilla.network.packets.login.clientbound.S2CLoginSuccess
import org.sandboxpowered.silica.vanilla.network.packets.login.serverbound.C2SEncryptionResponse
import org.sandboxpowered.silica.vanilla.network.packets.login.serverbound.C2SHandshakeRequest
import org.sandboxpowered.silica.vanilla.network.packets.login.serverbound.C2SLoginStart
import org.sandboxpowered.silica.vanilla.network.packets.play.clientbound.*
import org.sandboxpowered.silica.vanilla.network.packets.play.serverbound.*
import org.sandboxpowered.silica.vanilla.network.util.NetworkFlow
import org.sandboxpowered.silica.vanilla.network.util.PacketByteBuf
import java.util.function.Function
import java.util.function.Supplier
import kotlin.collections.set

@Suppress("unused")
enum class Protocol(private val id: Int, block: Builder.() -> Unit) {
    HANDSHAKE(-1, {
        server {
            0x00 packet ::C2SHandshakeRequest
        }
    }),
    PLAY(0, {
        server {
            0x00 packet ::C2STeleportConfirmation
            0x01 packet ::C2SQueryBlock
            0x02 packet ::C2SSetDifficulty
            0x03 packet ::C2SChatMessage
            0x04 packet ::C2SClientStatus
            0x05 packet ::C2SClientSettings
            0x06 packet ::C2STabComplete
            0x07 packet ::C2SClickWindowButton
            0x08 packet ::C2SClickWindow
            0x09 packet ::C2SCloseWindow
            0x0A packet ::C2SPluginChannel
            0x0B packet ::C2SEditBook
            0x0C packet ::C2SQueryEntity
            0x0D packet C2SInteractEntity::readFrom
            0x0E packet ::C2SGenerateStructure
            0x0F packet ::C2SKeepAliveServer
            0x10 packet ::C2SLockDifficulty
            0x11 packet ::C2SPlayerPosition
            0x12 packet ::C2SPlayerPositionAndRotation
            0x13 packet ::C2SPlayerRotation
            0x14 packet ::C2SPlayerMovement
            0x19 packet ::C2SPlayerAbilities
            0x1A packet ::C2SPlayerDigging
            0x17 packet ::C2SPickItem
            0x1B packet ::C2SEntityAction
            0x2E packet ::C2SPlayerBlockInteract
            0x25 packet ::C2SHeldItemChange
            0x28 packet ::C2SCreativeInventoryAction
            0x2C packet ::C2SHandSwingAnimation
            0x2F packet ::C2SUseItem
        }
        client {
            0x02 packet ::S2CSpawnLivingEntity
            0x04 packet ::S2CSpawnPlayer
            0x08 packet ::S2CAcknowledgePlayerDigging
            0x0C packet ::S2CBlockChange
            0x12 packetDeprecated ::S2CDeclareCommands
            0x14 packet ::S2CInitWindowItems
            0x18 packet ::S2CPluginChannel
            0x1B packet ::S2CEntityStatus
            0x20 packetDeprecated ::S2CWorldBorder
            0x21 packet ::S2CKeepAliveClient
            0x22 packet ::S2CChunkData
            0x25 packetDeprecated ::S2CUpdateLight
            0x26 packet ::S2CJoinGame
            0x29 packetDeprecated ::S2CUpdateEntityPosition
            0x2A packetDeprecated ::S2CUpdateEntityPositionRotation
            0x2B packetDeprecated ::S2CUpdateEntityRotation
            0x36 packetDeprecated ::S2CPlayerInfo
            0x38 packet ::S2CSetPlayerPositionAndLook
            0x39 packetDeprecated ::S2CUnlockRecipes
            0x3A packet ::S2CDestroyEntities
            0x48 packet ::S2CHeldItemChange
            0x49 packetDeprecated ::S2CUpdateChunkPosition
            0x65 packetDeprecated ::S2CDeclareRecipes
            0x66 packetDeprecated ::S2CDeclareTags
            0x0F packet ::S2CChatMessage
            0x3E packet ::S2CUpdateEntityHeadRotation
        }
    }),
    STATUS(1, {
        server {
            0x00 packet ::C2SStatusRequest
            0x01 packet ::C2SPingRequest
        }
        client {
            0x00 packet ::S2CStatusResponse
            0x01 packet ::S2CPongResponse
        }
    }),
    LOGIN(2, {
        server {
            0x00 packet ::C2SLoginStart
            0x01 packet ::C2SEncryptionResponse
        }
        client {
            0x00 packet ::S2CDisconnect
            0x01 packet ::S2CEncryptionRequest
            0x02 packet ::S2CLoginSuccess
        }
    });

    private val builder: Builder

    init {
        builder = Builder()
        block(builder)
    }

    companion object {
        val logger = getLogger()

        @JvmField
        val PROTOCOL_ATTRIBUTE_KEY: AttributeKey<Protocol> = AttributeKey.valueOf("protocol")
        private val PROTOCOL_BY_PACKET: MutableMap<Class<out Packet>, Protocol> = Maps.newHashMap()
        private val ID_2_PROTOCOL: Int2ObjectMap<Protocol> = Int2ObjectOpenHashMap()

        @JvmStatic
        fun getProtocolForPacket(packet: Packet): Protocol =
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
            val idToConstructor: Int2ObjectMap<Function<PacketBuffer, out PacketBase>> = Int2ObjectOpenHashMap()

            init {
                classToId.defaultReturnValue(-1)
            }

            fun getId(aClass: Class<out PacketBase>): Int = classToId.getInt(aClass)

            val allPackets: Iterable<Class<out PacketBase>>
                get() = Iterables.unmodifiableIterable(classToId.keys)

            fun createPacket(packetId: Int, buf: PacketBuffer): PacketBase? =
                if (!idToConstructor.containsKey(packetId)) null else idToConstructor[packetId].apply(buf)

            @Deprecated("use PacketBuffer constructor instead")
            inline infix fun <reified P : PacketBase> Int.packetDeprecated(packetSupplier: Supplier<P>) {
                packet { buf ->
                    val packet = packetSupplier.get()
                    packet.read(buf)
                    packet
                }
            }

            inline infix fun <reified P : PacketBase> Int.packet(packetSupplier: Function<PacketBuffer, P>) {
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