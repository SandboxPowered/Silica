package org.sandboxpowered.silica.network

import com.google.common.collect.ImmutableMap
import com.google.common.collect.Iterables
import com.google.common.collect.Maps
import io.netty.util.AttributeKey
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.apache.logging.log4j.LogManager
import org.sandboxpowered.silica.network.Protocol.Builder.Companion.newProtocol
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
import org.sandboxpowered.silica.network.play.serverbound.ClientPluginChannel
import org.sandboxpowered.silica.network.play.serverbound.ClientSettings
import org.sandboxpowered.silica.network.play.serverbound.KeepAliveServer
import org.sandboxpowered.silica.network.play.serverbound.TeleportConfirmation
import java.util.*
import java.util.function.Supplier

enum class Protocol(private val id: Int, builder: Builder) {
    HANDSHAKE(
        -1, newProtocol().addFlow(
            Flow.SERVERBOUND, Packets()
                .addPacket(0x00, ::HandshakeRequest)
        )
    ),
    PLAY(
        0, newProtocol()
            .addFlow(
                Flow.SERVERBOUND, Packets()
                    .addPacket(0x05, ::ClientSettings)
                    .addPacket(0x0B, ::ClientPluginChannel)
                    .addPacket(0x00, ::TeleportConfirmation)
                    .addPacket(0x10, ::KeepAliveServer)
            ).addFlow(
                Flow.CLIENTBOUND, Packets()
                    .addPacket(0x24, ::JoinGame)
                    .addPacket(0x3F, ::HeldItemChange)
                    .addPacket(0x5A, ::DeclareRecipes)
                    .addPacket(0x5B, ::DeclareTags)
                    .addPacket(0x1A, ::EntityStatus)
                    .addPacket(0x10, ::DeclareCommands)
                    .addPacket(0x34, ::SetPlayerPositionAndLook)
                    .addPacket(0x35, ::UnlockRecipes)
                    .addPacket(0x32, ::PlayerInfo)
                    .addPacket(0x40, ::UpdateChunkPosition)
                    .addPacket(0x20, ::ChunkData)
                    .addPacket(0x23, ::UpdateLight)
                    .addPacket(0x3D, ::WorldBorder)
                    .addPacket(0x1F, ::KeepAliveClient)
            )
    ),
    STATUS(
        1, newProtocol()
            .addFlow(
                Flow.SERVERBOUND, Packets()
                    .addPacket(0x00, ::StatusRequest)
                    .addPacket(0x01, ::PingRequest)
            ).addFlow(
                Flow.CLIENTBOUND, Packets()
                    .addPacket(0x00, ::StatusResponse)
                    .addPacket(0x01, ::PongResponse)
            )
    ),
    LOGIN(
        2, newProtocol()
            .addFlow(
                Flow.SERVERBOUND, Packets()
                    .addPacket(0x00, ::LoginStart)
                    .addPacket(0x01, ::EncryptionResponse)
            ).addFlow(
                Flow.CLIENTBOUND, Packets()
                    .addPacket(0x00, ::Disconnect)
                    .addPacket(0x01, ::EncryptionRequest)
                    .addPacket(0x02, ::LoginSuccess)
            )
    );

    companion object {
        @JvmField
        val PROTOCOL_ATTRIBUTE_KEY: AttributeKey<Protocol> = AttributeKey.valueOf("protocol")
        private val PROTOCOL_BY_PACKET: MutableMap<Class<out PacketBase>, Protocol> = Maps.newHashMap()
        private val ID_2_PROTOCOL: Int2ObjectMap<Protocol> = Int2ObjectOpenHashMap()

        @JvmStatic
        fun getProtocolForPacket(packet: PacketBase): Protocol {
            return PROTOCOL_BY_PACKET[packet.javaClass]
                ?: throw NullPointerException("No protocol found for " + packet.javaClass)
        }

        @JvmStatic
        fun getProtocolFromId(id: Int): Protocol {
            return ID_2_PROTOCOL[id]
        }

        init {
            for (protocol in values()) {
                protocol.packets.forEach { (_: Flow, packetSet: Packets) ->
                    packetSet.allPackets.forEach { packetClass: Class<out PacketBase> ->
                        PROTOCOL_BY_PACKET[packetClass] = protocol
                    }
                }
                ID_2_PROTOCOL[protocol.id] = protocol
            }
        }
    }

    private val packets: Map<Flow, Packets>
    fun getPacketId(flow: Flow, msg: PacketBase): Int {
        return packets[flow]?.getId(msg.javaClass) ?: -1
    }

    fun createPacket(flow: Flow, packetId: Int): PacketBase? {
        return packets[flow]?.createPacket(packetId)
    }

    class Builder {
        companion object {
            fun newProtocol(): Builder {
                return Builder()
            }
        }

        val packets: MutableMap<Flow, Packets> = EnumMap(Flow::class.java)

        fun addFlow(flow: Flow, packetSet: Packets): Builder {
            packets[flow] = packetSet
            return this
        }
    }

    class Packets {
        val classToId: Object2IntMap<Class<out PacketBase>> = Object2IntOpenHashMap()
        val idToConstructor: Int2ObjectMap<Supplier<out PacketBase>> = Int2ObjectOpenHashMap()
        private val ignoredIds: IntList = IntArrayList()

        init {
            classToId.defaultReturnValue(-1)
        }

        inline fun <reified P : PacketBase> addPacket(targetId: Int, supplier: Supplier<P>): Packets {
            val id = classToId.put(P::class.java, targetId)
            return if (id != -1) {
                val string = "Packet ${P::class.java} is already registered to ID $id"
                LogManager.getLogger().fatal(string)
                throw IllegalArgumentException(string)
            } else {
                idToConstructor[targetId] = supplier
                this
            }
        }

        fun ignore(vararg ids: Int): Packets {
            for (i in ids) {
                ignoredIds.add(i)
            }
            return this
        }

        fun getId(aClass: Class<out PacketBase?>?): Int {
            return classToId.getInt(aClass)
        }

        val allPackets: Iterable<Class<out PacketBase>>
            get() = Iterables.unmodifiableIterable(classToId.keys)

        fun createPacket(packetId: Int): PacketBase? {
            return if (ignoredIds.contains(packetId) || !idToConstructor.containsKey(packetId)) null else idToConstructor[packetId].get()
        }
    }

    init {
        packets = ImmutableMap.copyOf(builder.packets)
    }
}