package org.sandboxpowered.silica.client.util;

import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.activity.Activity;
import org.sandboxpowered.silica.client.Silica;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.function.Consumer;


public class DiscordManager {

    private final Core core;

    private Instant timestamp;

    public DiscordManager() {
        Silica.LOG.debug("Starting Discord Integration");
        resetTimestamp();
        core = setup();
    }

    private boolean isEnabled() {
        return core != null;
    }

    private Core setup() {
        File discordLibrary = null;
        try {
            discordLibrary = DiscordUtil.getDiscordLibrary();
        } catch (IOException e) {
            Silica.LOG.error("Failed to get Discord library", e);
            return null;
        }
        Core.init(discordLibrary);
        try (CreateParams params = new CreateParams()) {
            params.setClientID(765951222082437152L);
            params.setFlags(CreateParams.getDefaultFlags() | 1 | 2);
            return new Core(params);
        }
    }

    public void resetTimestamp() {
        timestamp = Instant.now();
    }

    public void updateActivity(Consumer<Activity> consumer) {
        if (isEnabled()) {
            try (Activity activity = new Activity()) {
                setDefaultActivity(activity);
                consumer.accept(activity);
                core.activityManager().updateActivity(activity);
            }
        }
    }

    private void setDefaultActivity(Activity activity) {
        activity.assets().setLargeImage("logo");
        activity.assets().setLargeText("Sandbox Silica v0.0.1");
        activity.timestamps().setStart(timestamp);
    }

    public void update() {
        if (isEnabled())
            core.runCallbacks();
    }

    public void close() {
        if (isEnabled())
            core.close();
    }
}