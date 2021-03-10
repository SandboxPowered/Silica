package org.sandboxpowered.silica.network;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import com.mojang.authlib.GameProfile;
import org.sandboxpowered.silica.network.login.clientbound.LoginSuccess;
import org.sandboxpowered.silica.network.login.serverbound.EncryptionResponse;
import org.sandboxpowered.silica.server.NetworkActor;
import org.sandboxpowered.silica.server.SilicaServer;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

public class Connection {
    private final SilicaServer server;
    private final ActorRef<? super NetworkActor.Command.CreateConnection> network;
    private final Scheduler scheduler;
    private GameProfile profile;
    private SecretKey secretKey;
    private PacketHandler packetHandler;
    public int ping;

    public Connection(SilicaServer server, ActorRef<? super NetworkActor.Command.CreateConnection> network, Scheduler scheduler) {
        this.server = server;
        this.network = network;
        this.scheduler = scheduler;
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
        System.out.println("Sending");
        AskPattern.ask(
                network,
                ref -> new NetworkActor.Command.CreateConnection(profile, this.packetHandler, ref),
                Duration.ofSeconds(3),
                scheduler
        ).whenComplete((reply, failure) -> {
            if (failure != null) System.out.println("Couldn't create connection : " + failure.getMessage());
            else if (reply instanceof Boolean) {
                boolean result = (Boolean) reply;
                System.out.println("Created connection: " + result);
            }
        });
    }

    public void calculatePing(long id) {
        ping = (int) (System.currentTimeMillis() - id);
    }
}
