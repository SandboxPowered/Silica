package org.sandboxpowered.silica.network.play.clientbound;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayContext;

import java.util.UUID;

public class PlayerInfo implements PacketPlay {
    private int action;

    private UUID[] uuids;

    private String[] names; // Action 0
    private PropertyMap[] propertyMaps; // Action 0
    private int[] gamemodes; // Action 0,1
    private int[] pings; // Action 0,2

    public PlayerInfo() {
    }

    public PlayerInfo(int action, UUID[] uuids, String[] names, PropertyMap[] propertyMaps, int[] gamemodes, int[] pings) {
        this.action = action;
        this.uuids = uuids;
        this.names = names;
        this.propertyMaps = propertyMaps;
        this.gamemodes = gamemodes;
        this.pings = pings;
    }

    public static PlayerInfo addPlayer(GameProfile[] profiles, int[] gamemodes, int[] pings) {
        UUID[] uuids = new UUID[profiles.length];
        String[] names = new String[profiles.length];
        PropertyMap[] propertyMaps = new PropertyMap[profiles.length];
        for (int i = 0; i < profiles.length; i++) {
            GameProfile profile = profiles[i];
            uuids[i] = profile.getId();
            names[i] = profile.getName();
            propertyMaps[i] = profile.getProperties();
        }
        return new PlayerInfo(0, uuids, names, propertyMaps, gamemodes, pings);
    }

    public static PlayerInfo updateLatency(UUID[] uuids, int[] pings) {
        return new PlayerInfo(2, uuids, new String[0], new PropertyMap[0], new int[0], pings);
    }

    public static PlayerInfo removePlayer(UUID[] uuids) {
        return new PlayerInfo(4, uuids, new String[0], new PropertyMap[0], new int[0], new int[0]);
    }

    @Override
    public void read(PacketByteBuf buf) {
        action = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(action);
        buf.writeVarInt(uuids.length);

        for (int i = 0; i < uuids.length; i++) {
            buf.writeUUID(uuids[i]);
            if (action == 0) {
                buf.writeString(names[i], 16);
                PropertyMap map = propertyMaps[i];
                buf.writeVarInt(map.size());
                map.forEach((s, property) -> {
                    buf.writeString(property.getName());
                    buf.writeString(property.getValue());
                    buf.writeBoolean(property.hasSignature());
                    if (property.hasSignature()) {
                        buf.writeString(property.getSignature());
                    }
                });
                buf.writeVarInt(gamemodes[i]);
                buf.writeVarInt(pings[i]);
                buf.writeBoolean(false);
            } else if (action == 1) {
                buf.writeVarInt(gamemodes[i]);
            } else if (action == 2) {
                buf.writeVarInt(pings[i]);
            } else if (action == 3) {
                buf.writeBoolean(false);
            }
        }
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}