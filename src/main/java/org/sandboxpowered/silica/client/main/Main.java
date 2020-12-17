package org.sandboxpowered.silica.client.main;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sandboxpowered.silica.client.Silica;

import java.util.List;

public class Main {
    public static final Logger LOG = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        OptionParser optionSpec = new OptionParser();
        optionSpec.allowsUnrecognizedOptions();
        OptionSpec<Integer> widthSpec = optionSpec.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo(1000);
        OptionSpec<Integer> heightSpec = optionSpec.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo(563);
        OptionSpec<String> unknownOptionsSpec = optionSpec.nonOptions();
        OptionSet options = optionSpec.parse(args);
        List<String> unknownOptions = options.valuesOf(unknownOptionsSpec);
        if (!unknownOptions.isEmpty()) {
            LOG.warn("Ignoring arguments: " + unknownOptions);
        }

        try {
            new Silica(new Silica.Args(options.valueOf(widthSpec), options.valueOf(heightSpec))).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}