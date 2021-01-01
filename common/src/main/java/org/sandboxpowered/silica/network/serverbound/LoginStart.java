package org.sandboxpowered.silica.network.serverbound;

import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.clientbound.EncryptionRequest;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

public class LoginStart implements Packet {
    public static final byte[] verification = new byte[4];
    public static KeyPair KEYPAIR;

    static {
        ThreadLocalRandom.current().nextBytes(verification);
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            KEYPAIR = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String username;

    @Override
    public void read(PacketByteBuf buf) {
        username = buf.readString(16);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(username);
    }

    @Override
    public void handle(PacketHandler packetHandler) {
        packetHandler.sendPacket(new EncryptionRequest("", KEYPAIR.getPublic().getEncoded(), verification));
    }
}