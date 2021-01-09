package org.sandboxpowered.silica.network;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import io.netty.util.AttributeKey;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.sandboxpowered.silica.network.handshake.clientbound.PingRequest;
import org.sandboxpowered.silica.network.handshake.clientbound.StatusRequest;
import org.sandboxpowered.silica.network.handshake.serverbound.PongResponse;
import org.sandboxpowered.silica.network.handshake.serverbound.StatusResponse;
import org.sandboxpowered.silica.network.login.clientbound.Disconnect;
import org.sandboxpowered.silica.network.login.clientbound.EncryptionRequest;
import org.sandboxpowered.silica.network.login.clientbound.LoginSuccess;
import org.sandboxpowered.silica.network.login.serverbound.EncryptionResponse;
import org.sandboxpowered.silica.network.login.serverbound.HandshakeRequest;
import org.sandboxpowered.silica.network.login.serverbound.LoginStart;
import org.sandboxpowered.silica.network.play.clientbound.*;
import org.sandboxpowered.silica.network.play.serverbound.ClientPluginChannel;
import org.sandboxpowered.silica.network.play.serverbound.ClientSettings;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public enum Protocol {
    HANDSHAKE(-1, newProtocol().addFlow(Flow.SERVERBOUND, new Packets()
            .addPacket(0x00, HandshakeRequest.class, HandshakeRequest::new)
    )),
    PLAY(0, newProtocol()
            .addFlow(Flow.SERVERBOUND, new Packets()
                    .addPacket(0x05, ClientSettings.class, ClientSettings::new)
                    .addPacket(0x0B, ClientPluginChannel.class, ClientPluginChannel::new)
            ).addFlow(Flow.CLIENTBOUND, new Packets()
                    .addPacket(0x24, JoinGame.class, JoinGame::new)
                    .addPacket(0x3F, HeldItemChange.class, HeldItemChange::new)
                    .addPacket(0x5A, DeclareRecipes.class, DeclareRecipes::new)
                    .addPacket(0x5B, DeclareTags.class, DeclareTags::new)
                    .addPacket(0x1A, EntityStatus.class, EntityStatus::new)
                    .addPacket(0x10, DeclareCommands.class, DeclareCommands::new)
                    .addPacket(0x35, UnlockRecipes.class, UnlockRecipes::new)
            )
    ),
    STATUS(1, newProtocol()
            .addFlow(Flow.SERVERBOUND, new Packets()
                    .addPacket(0x00, StatusRequest.class, StatusRequest::new)
                    .addPacket(0x01, PingRequest.class, PingRequest::new)
            ).addFlow(Flow.CLIENTBOUND, new Packets()
                    .addPacket(0x00, StatusResponse.class, StatusResponse::new)
                    .addPacket(0x01, PongResponse.class, PongResponse::new)
            )),
    LOGIN(2, newProtocol()
            .addFlow(Flow.SERVERBOUND, new Packets()
                    .addPacket(0x00, LoginStart.class, LoginStart::new)
                    .addPacket(0x01, EncryptionResponse.class, EncryptionResponse::new)
            ).addFlow(Flow.CLIENTBOUND, new Packets()
                    .addPacket(0x00, Disconnect.class, Disconnect::new)
                    .addPacket(0x01, EncryptionRequest.class, EncryptionRequest::new)
                    .addPacket(0x02, LoginSuccess.class, LoginSuccess::new)
            )
    );


    public static final AttributeKey<Protocol> PROTOCOL_ATTRIBUTE_KEY = AttributeKey.valueOf("protocol");
    private static final Map<Class<? extends Packet>, Protocol> PROTOCOL_BY_PACKET = Maps.newHashMap();
    private static final Int2ObjectMap<Protocol> ID_2_PROTOCOL = new Int2ObjectOpenHashMap<>();

    static {
        for (Protocol protocol : values()) {
            protocol.packets.forEach((packetFlow, packetSet) -> {
                packetSet.getAllPackets().forEach((class_) -> {
                    PROTOCOL_BY_PACKET.put(class_, protocol);
                });
            });
            ID_2_PROTOCOL.put(protocol.id, protocol);
        }
    }

    private final int id;
    private final Map<Flow, Packets> packets;

    Protocol(int id, Builder builder) {
        this.id = id;
        this.packets = ImmutableMap.copyOf(builder.packets);
    }

    public static Builder newProtocol() {
        return new Builder();
    }

    public static Protocol getProtocolForPacket(Packet packet) {
        Protocol protocol = PROTOCOL_BY_PACKET.get(packet.getClass());
        if (protocol == null)
            throw new NullPointerException("No protocol found for " + packet.getClass());
        return protocol;
    }

    public static Protocol getProtocolFromId(int id) {
        return ID_2_PROTOCOL.get(id);
    }

    public int getPacketId(Flow flow, Packet msg) {
        return packets.get(flow).getId(msg.getClass());
    }

    public Packet createPacket(Flow flow, int packetId) {
        return packets.get(flow).createPacket(packetId);
    }

    public static class Builder {
        private final Map<Flow, Packets> packets = new EnumMap<>(Flow.class);

        public Builder addFlow(Flow flow, Packets packetSet) {
            this.packets.put(flow, packetSet);
            return this;
        }
    }

    public static class Packets {
        private final Object2IntMap<Class<? extends Packet>> classToId = new Object2IntOpenHashMap<>() {{
            defaultReturnValue(-1);
        }};
        private final Int2ObjectMap<Supplier<? extends Packet>> idToConstructor = new Int2ObjectOpenHashMap<>();

        public <P extends Packet> Packets addPacket(int targetId, Class<P> pClass, Supplier<P> supplier) {
            int j = this.classToId.put(pClass, targetId);
            if (j != -1) {
                String string = "Packet " + pClass + " is already registered to ID " + j;
                LogManager.getLogger().fatal(string);
                throw new IllegalArgumentException(string);
            } else {
                this.idToConstructor.put(targetId, supplier);
                return this;
            }
        }

        public int getId(Class<? extends Packet> aClass) {
            return classToId.getInt(aClass);
        }

        public Iterable<Class<? extends Packet>> getAllPackets() {
            return Iterables.unmodifiableIterable(this.classToId.keySet());
        }

        public Packet createPacket(int packetId) {
            if (!idToConstructor.containsKey(packetId))
                return null;
            return idToConstructor.get(packetId).get();
        }
    }
}