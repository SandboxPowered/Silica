package org.sandboxpowered.silica.network;

import com.mojang.authlib.GameProfile;
import org.sandboxpowered.silica.network.login.serverbound.EncryptionResponse;
import org.sandboxpowered.silica.server.SilicaServer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.UUID;

public class Connection {
    private final SilicaServer server;
    private GameProfile profile;
    private SecretKey secretKey;

    public Connection(SilicaServer server) {
        this.server = server;
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
    }
}
