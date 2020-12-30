package org.sandboxpowered.silica.server;

import org.sandboxpowered.api.entity.player.PlayerEntity;
import org.sandboxpowered.api.server.Server;
import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.api.world.World;
import org.sandboxpowered.silica.world.SilicaWorld;

import java.util.stream.Stream;

public class SilicaServer implements Server {
    private SilicaWorld world;

    @Override
    public World getWorld(Identity identity) {
        return world;
    }

    @Override
    public Stream<PlayerEntity> getPlayers() {
        return Stream.empty();
    }
}
