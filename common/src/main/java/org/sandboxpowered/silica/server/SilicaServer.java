package org.sandboxpowered.silica.server;

import org.checkerframework.checker.units.qual.C;
import org.sandboxpowered.api.entity.player.PlayerEntity;
import org.sandboxpowered.api.server.Server;
import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.api.world.World;
import org.sandboxpowered.silica.command.Commands;
import org.sandboxpowered.silica.world.SilicaWorld;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.stream.Stream;

public class SilicaServer implements Server {
    private final KeyPair encryptionKeyPair;
    private final byte[] verificationArray = new byte[4];
    private final Random serverRandom = new Random();
    private SilicaWorld world;
    private Commands commands;

    public SilicaServer() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            encryptionKeyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        serverRandom.nextBytes(verificationArray);
        commands = new Commands();
    }

    public Commands getCommands() {
        return commands;
    }

    public byte[] getVerificationArray() {
        return verificationArray;
    }

    public KeyPair getKeyPair() {
        return encryptionKeyPair;
    }

    @Override
    public World getWorld(Identity identity) {
        return world;
    }

    @Override
    public Stream<PlayerEntity> getPlayers() {
        return Stream.empty();
    }


}
