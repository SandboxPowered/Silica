package org.sandboxpowered.silica.client.util;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
        File filePath = new File("./cache", name + suffix);
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
}