package org.sandboxpowered.silica.client.util;
import de.jcm.discordgamesdk.Core;
import de.jcm.discordgamesdk.CreateParams;
import de.jcm.discordgamesdk.Result;
import de.jcm.discordgamesdk.activity.Activity;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.sandboxpowered.silica.client.Silica;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
public class DiscordUtil {

    @NotNull
    public static File getDiscordLibrary() throws IOException {
        // Find out which name Discord's library has (.dll for Windows, .so for Linux)
        String name = "discord_game_sdk";
        String suffix;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            suffix = ".dll";
        } else {
            suffix = ".so";
        }
        File filePath = new File("./caches", name + suffix);
        if (filePath.exists())
            return filePath;

        // Path of Discord's library inside the ZIP
        String zipPath = "lib/x86_64/" + name + suffix;

        // Open the URL as a ZipInputStream
        URL downloadUrl = new URL("https://dl-game-sdk.discordapp.net/latest/discord_game_sdk.zip");
        ZipInputStream zin = new ZipInputStream(downloadUrl.openStream());
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            if (entry.getName().equals(zipPath)) {
                File tempDir = new File("./cache");
                if (!tempDir.mkdirs())
                    throw new IOException("Cannot create temporary directory");

                File temp = new File(tempDir, name + suffix);

                FileOutputStream fout = new FileOutputStream(temp);
                IOUtils.copy(zin, fout);
                fout.close();

                zin.close();

                return temp;
            }
            // next entry
            zin.closeEntry();
        }
        zin.close();
        // We couldn't find the library inside the ZIP
        throw new FileNotFoundException("Unable to locate discord sdk");
    }

    private static Core core = null;

    public static boolean isDiscordActive() {
        return core != null;
    }

    public static Core getCore() {
        return core;
    }

    public static void close() {
        if (core != null)
            core.close();
    }

    public static void setup() {
        Silica.LOG.debug("Starting Discord Integration");
        File discordLibrary = null;
        try {
            discordLibrary = DiscordUtil.getDiscordLibrary();
        } catch (IOException e) {
            Silica.LOG.error("Failed to get Discord library", e);
        }
        if (discordLibrary == null)
            return;
        Core.init(discordLibrary);
        try (CreateParams params = new CreateParams()) {
            params.setClientID(765951222082437152L);
            params.setFlags(CreateParams.getDefaultFlags() | 2);
            core = new Core(params);
        }
    }

    public static void runCallbacks() {
        if (core != null)
            core.runCallbacks();
    }

    public static void updateActivity(Activity activity, Consumer<Result> resultConsumer) {
        if (core != null)
            core.activityManager().updateActivity(activity, resultConsumer);
        resultConsumer.accept(Result.SERVICE_UNAVAILABLE);
    }

    public static CompletableFuture<Result> updateActivityFuture(Activity activity) {
        CompletableFuture<Result> future = new CompletableFuture<>();
        updateActivity(activity, future::complete);
        return future;
    }

    public static void updateActivity(Activity activity) {
        if (core != null)
            core.activityManager().updateActivity(activity);
    }
}