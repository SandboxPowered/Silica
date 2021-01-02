package org.sandboxpowered.silica.server.main;

import org.sandboxpowered.silica.server.DedicatedServer;

public class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("actor")) new DedicatedServer().runActors();
        else new DedicatedServer().oldRun();
    }
}