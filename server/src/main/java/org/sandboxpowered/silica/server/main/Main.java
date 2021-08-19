package org.sandboxpowered.silica.server.main;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.logging.log4j.LogManager;
import org.sandboxpowered.silica.server.DedicatedServer;
import org.sandboxpowered.silica.server.ServerEula;
import org.sandboxpowered.silica.server.ServerProperties;

import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        OptionParser spec = new OptionParser();
        spec.allowsUnrecognizedOptions();
        spec.accepts("initSettings");
        OptionSet set = spec.parse(args);
        ServerEula eula = new ServerEula(Paths.get("eula.txt"));
        if (set.has("initSettings")) {
            ServerProperties.fromFile(Paths.get("server.properties"));
        } else {
            if (!eula.getAgreed()) {
                LogManager.getLogger("Minecraft EULA").error("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            new DedicatedServer().run();
        }
    }
}