package org.sandboxpowered.silica.client;

import com.google.common.base.Joiner;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.sandboxpowered.silica.client.resources.DirectoryResourceLoader;
import org.sandboxpowered.silica.client.resources.ResourceManager;
import org.sandboxpowered.silica.client.resources.ZIPResourceLoader;
import org.sandboxpowered.silica.client.util.FileFilters;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.opengl.GL11.*;

public class Silica implements Runnable {
    public static final Logger LOG = LogManager.getLogger(Silica.class);

    private final Window window;

    private final ResourceManager manager;


    public Silica(Args args) {
        List<String> list = new ArrayList<>();
        glfwSetErrorCallback((i, l) ->
                list.add(String.format("GLFW error during init: [0x%X]%s", i, l))
        );
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW, errors: " + Joiner.on(",").join(list));
        }
        for (String string : list) {
            LOG.error("GLFW error collected during initialization: {}", string);
        }

        manager = new ResourceManager();

        try {
            URL url = Silica.class.getResource("/log4j2.xml").toURI().resolve(".").toURL();
            Path path = asPath(url);
            if (path != null) {
                if (Files.isDirectory(path)) {
                    manager.add(new DirectoryResourceLoader(path.toFile()));
                } else {
                    manager.add(new ZIPResourceLoader(path.toFile()));
                }
            }
        } catch (IOException | URISyntaxException e) {
            LOG.error("Error loading default resources", e);
        }

        File resourcePacks = new File("resourcepacks");

        if (!resourcePacks.exists())
            resourcePacks.mkdirs();

        Collection<File> files = FileUtils.listFiles(resourcePacks, FileFilters.ZIP.or(FileFilters.JAR), new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return false;
            }

            @Override
            public boolean accept(File dir, String name) {
                return false;
            }
        });
        files.forEach(file -> {
            try {
                if (file.isDirectory()) {
                    manager.add(new DirectoryResourceLoader(file));
                } else {
                    manager.add(new ZIPResourceLoader(file));
                }
            } catch (IOException e) {
                LOG.error("Failed loading resource pack {}", file.getName());
            }
        });

        LOG.debug("Loaded namespaces: [{}]", StringUtils.join(manager.getNamespaces(), ","));

        window = new Window("Sandbox Silica", args.width, args.height);

        GL.createCapabilities();

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while (!window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            window.update();
        }

        close();
    }

    public static Path asPath(URL url) {
        if (url.getProtocol().equals("file")) {
            return asFile(url).toPath();
        } else {
            try {
                return Paths.get(url.toURI());
            } catch (URISyntaxException var2) {
                LOG.error("Error finding path of url", var2);
                return null;
            }
        }
    }

    public static File asFile(URL url) {
        try {
            return new File(url.toURI());
        } catch (URISyntaxException var2) {
            LOG.error("Error finding file of url", var2);
            return null;
        }
    }

    public void close() {
        window.cleanup();
    }

    @Override
    public void run() {

    }

    public static class Args {
        public final int width, height;

        public Args(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}