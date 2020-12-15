package org.sandboxpowered.silica.client;

import de.jcm.discordgamesdk.activity.Activity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sandboxpowered.silica.client.util.DiscordUtil;

import java.time.Instant;

public class Silica {

    public static final Logger LOG = LogManager.getFormatterLogger();

    public void run() {
        DiscordUtil.setup();

        if (DiscordUtil.isDiscordActive()) {
            try (Activity activity = new Activity()) {
                activity.setState("Loading");

                activity.timestamps().setStart(Instant.now());

                activity.assets().setLargeImage("logo");
                activity.assets().setLargeText("Sandbox Silica v0.0.1");

                DiscordUtil.getCore().activityManager().updateActivity(activity);
            }

            while (true) {
                DiscordUtil.getCore().runCallbacks();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        DiscordUtil.close();
    }
}
