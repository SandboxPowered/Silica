package org.sandboxpowered.silica.network;

import com.mojang.authlib.GameProfile;
import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.silica.nbt.CompoundTag;
import org.sandboxpowered.silica.network.login.clientbound.LoginSuccess;
import org.sandboxpowered.silica.network.login.serverbound.EncryptionResponse;
import org.sandboxpowered.silica.network.play.clientbound.*;
import org.sandboxpowered.silica.server.SilicaServer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

public class Connection {
    private final SilicaServer server;
    private GameProfile profile;
    private SecretKey secretKey;
    private PacketHandler packetHandler;

    public Connection(SilicaServer server) {
        this.server = server;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public void setPacketHandler(PacketHandler packetHandler) {
        this.packetHandler = packetHandler;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public SilicaServer getServer() {
        return server;
    }

    public void handleEncryptionResponse(EncryptionResponse encryptionResponse) {
        PrivateKey privateKey = server.getKeyPair().getPrivate();
        try {
            if (!Arrays.equals(server.getVerificationArray(), encryptionResponse.getVerificationToken(privateKey))) {
                throw new IllegalStateException("Protocol error");
            }

            this.secretKey = encryptionResponse.getSecretKey(privateKey);
            Cipher cipher = encryptionResponse.getCipher(2, this.secretKey);
            Cipher cipher2 = encryptionResponse.getCipher(1, this.secretKey);
        } catch (EncryptionException e) {
            e.printStackTrace();
        }
    }

    public void handleLoginStart(String username) {
        this.profile = new GameProfile(UUID.randomUUID(), username);
//        packetHandler.sendPacket(new EncryptionRequest("", server.getKeyPair().getPublic().getEncoded(), server.getVerificationArray()));
        packetHandler.sendPacket(new LoginSuccess(getProfile().getId(), username));
        packetHandler.setProtocol(Protocol.PLAY);

        Identity overworld = Identity.of("minecraft", "overworld");

        CompoundTag codec = new CompoundTag();
        CompoundTag dimReg = new CompoundTag();

        dimReg.setString("type", "minecraft:dimension_type");
        CompoundTag overworldTypeEntry = new CompoundTag();

        overworldTypeEntry.setString("name", overworld.toString());
        overworldTypeEntry.setInt("id", 0);
        CompoundTag overworldType = new CompoundTag();

        overworldType.setBoolean("piglin_safe", false);
        overworldType.setBoolean("natural", true);
        overworldType.setFloat("ambient_light", 1);
        overworldType.setString("infiniburn", "");
        overworldType.setBoolean("respawn_anchor_works", false);
        overworldType.setBoolean("has_skylight", true);
        overworldType.setBoolean("bed_works", true);
        overworldType.setString("effects", "minecraft:overworld");
        overworldType.setBoolean("has_raids", true);
        overworldType.setInt("logical_height", 256);
        overworldType.setFloat("coordinate_scale", 1);
        overworldType.setBoolean("ultrawarm", false);
        overworldType.setBoolean("has_ceiling", false);

        overworldTypeEntry.setTag("element", overworldType);

        dimReg.setList("value", Collections.singletonList(overworldTypeEntry));

        CompoundTag biomeReg = new CompoundTag();

        biomeReg.setString("type", "minecraft:worldgen/biome");

        CompoundTag plainsBiomeEntry = new CompoundTag();
        plainsBiomeEntry.setString("name", "minecraft:plains");
        plainsBiomeEntry.setInt("id", 0);
        CompoundTag plains = new CompoundTag();

        plains.setString("precipitation", "rain");
        plains.setFloat("depth", 0);
        plains.setFloat("temperature", 0);
        plains.setFloat("scale", 1);
        plains.setFloat("downfall", 1);
        plains.setString("category", "plains");
        CompoundTag effects = new CompoundTag();

        effects.setInt("sky_color", 8364543);
        effects.setInt("water_fog_color", 8364543);
        effects.setInt("fog_color", 8364543);
        effects.setInt("water_color", 8364543);

        plains.setTag("effects", effects);

        plainsBiomeEntry.setTag("element", plains);
        biomeReg.setList("value", Collections.singletonList(plainsBiomeEntry));

        codec.setTag("minecraft:dimension_type", dimReg);
        codec.setTag("minecraft:worldgen/biome", biomeReg);


        packetHandler.sendPacket(new JoinGame(
                0,
                false,
                (short) 1,
                (short) -1,
                1,
                new Identity[]{overworld},
                codec,
                overworldType,
                overworld,
                0,
                20,
                4,
                false,
                true,
                false,
                true
        ));

        packetHandler.sendPacket(new HeldItemChange((byte) 0));
        packetHandler.sendPacket(new DeclareRecipes());
        packetHandler.sendPacket(new DeclareTags());
        packetHandler.sendPacket(new EntityStatus());
        packetHandler.sendPacket(new DeclareCommands());
        packetHandler.sendPacket(new UnlockRecipes());
        packetHandler.sendPacket(new SetPlayerPositionAndLook(8, 8, 8, 0, 0, (byte) 0, 0));
        packetHandler.sendPacket(new PlayerInfo(0));
        packetHandler.sendPacket(new PlayerInfo(2));
        packetHandler.sendPacket(new UpdateChunkPosition(0, 0));

        for(int x = -2; x < 3; x++) {
            for(int z = -2; z < 3; z++) {
                packetHandler.sendPacket(new UpdateLight(x,z, true));
            }
        }

        packetHandler.sendPacket(new WorldBorder());
    }
}
