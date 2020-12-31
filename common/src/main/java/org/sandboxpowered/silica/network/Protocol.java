package org.sandboxpowered.silica.network;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import io.netty.util.AttributeKey;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.sandboxpowered.silica.network.clientbound.StatusResponse;
import org.sandboxpowered.silica.network.serverbound.HandshakeRequest;
import org.sandboxpowered.silica.network.serverbound.StatusRequest;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public enum Protocol {
    HANDSHAKE(-1, newProtocol().addFlow(Flow.SERVERBOUND, new Packets()
            .addPacket(HandshakeRequest.class, HandshakeRequest::new)
    )),
    PLAY(0, newProtocol()),
    STATUS(1, newProtocol()
            .addFlow(Flow.SERVERBOUND, new Packets()
                    .addPacket(StatusRequest.class, StatusRequest::new)
            ).addFlow(Flow.CLIENTBOUND, new Packets()
                    .addPacket(StatusResponse.class, StatusResponse::new)
            )),
    LOGIN(2, newProtocol());
    public static final AttributeKey<Protocol> PROTOCOL_ATTRIBUTE_KEY = AttributeKey.valueOf("protocol");
    private static final Map<Class<? extends Packet>, Protocol> PROTOCOL_BY_PACKET = Maps.newHashMap();

    static {
        for (Protocol protocol : values()) {
            protocol.packets.forEach((packetFlow, packetSet) -> {
                packetSet.getAllPackets().forEach((class_) -> {
                    PROTOCOL_BY_PACKET.put(class_, protocol);
                });
            });
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
        return PROTOCOL_BY_PACKET.get(packet.getClass());
    }

    public int getPacketId(Flow flow, Packet msg) {
        return packets.get(flow).getId(msg.getClass());
    }

    public Packet createPacket(Flow flow, int packetId) {
        return packets.get(flow).idToConstructor.get(packetId).get();
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
        private final List<Supplier<? extends Packet>> idToConstructor = new ArrayList<>();

        public <P extends Packet> Packets addPacket(Class<P> pClass, Supplier<P> supplier) {
            int i = this.idToConstructor.size();
            int j = this.classToId.put(pClass, i);
            if (j != -1) {
                String string = "Packet " + pClass + " is already registered to ID " + j;
                LogManager.getLogger().fatal(string);
                throw new IllegalArgumentException(string);
            } else {
                this.idToConstructor.add(supplier);
                return this;
            }
        }

        public int getId(Class<? extends Packet> aClass) {
            return classToId.getInt(aClass);
        }

        public Iterable<Class<? extends Packet>> getAllPackets() {
            return Iterables.unmodifiableIterable(this.classToId.keySet());
        }
    }
}